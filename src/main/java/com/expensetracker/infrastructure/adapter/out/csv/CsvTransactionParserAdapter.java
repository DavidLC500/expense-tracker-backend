package com.expensetracker.infrastructure.adapter.out.csv;

import com.expensetracker.application.port.out.BankProfile;
import com.expensetracker.application.port.out.CsvParseOutcome;
import com.expensetracker.application.port.out.CsvTransactionParser;
import com.expensetracker.application.port.out.RowError;
import com.expensetracker.application.port.out.RowErrorCode;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.Transaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

/**
 * Parsea un CSV de movimientos bancarios con Apache Commons CSV, según el {@link BankProfile}
 * indicado. Cada fila se procesa de forma independiente: una fila inválida se registra como {@link
 * RowError} y no interrumpe el resto del fichero.
 */
@Component
public class CsvTransactionParserAdapter implements CsvTransactionParser {

  // Debe coincidir con transactions.concept VARCHAR(500) (V3__create_transactions.sql). Validarlo
  // aquí, por fila, evita que una fila con un concepto demasiado largo tumbe toda la petición con
  // un error de base de datos en vez de reportarse como el error de esa fila concreta.
  private static final int MAX_CONCEPT_LENGTH = 500;

  // Debe coincidir con transactions.amount NUMERIC(14,2): 14 dígitos, 2 de ellos decimales. Mismo
  // motivo que MAX_CONCEPT_LENGTH: un importe que no cabe es un error de su fila, no un 409.
  private static final int MAX_AMOUNT_INTEGER_DIGITS = 12;

  private static final char BYTE_ORDER_MARK = 0xFEFF;

  // setAllowMissingColumnNames: muchos bancos exportan una coma final ("fecha,concepto,importe,"),
  // que crea una columna sin nombre; sin esto Commons CSV rechaza el fichero entero.
  private static final CSVFormat CSV_FORMAT =
      CSVFormat.DEFAULT
          .builder()
          .setHeader()
          .setSkipHeaderRecord(true)
          .setAllowMissingColumnNames(true)
          .get();

  private final Clock clock;

  public CsvTransactionParserAdapter(Clock clock) {
    this.clock = clock;
  }

  @Override
  public CsvParseOutcome parse(InputStream csvContent, BankProfile profile) {
    List<Transaction> transactions = new ArrayList<>();
    List<RowError> errors = new ArrayList<>();
    DateTimeFormatter dateFormatter =
        DateTimeFormatter.ofPattern(profile.datePattern()).withResolverStyle(ResolverStyle.STRICT);

    try (Reader reader =
        skipByteOrderMark(new InputStreamReader(csvContent, StandardCharsets.UTF_8))) {
      CSVParser parser;
      try {
        parser = CSV_FORMAT.parse(reader);
      } catch (IOException | UncheckedIOException | IllegalArgumentException malformedHeader) {
        errors.add(new RowError(1, RowErrorCode.MALFORMED_CSV, malformedHeader.getMessage()));
        return new CsvParseOutcome(transactions, errors);
      }
      try (parser) {
        parseRecords(parser, profile, dateFormatter, transactions, errors);
      }
    } catch (IOException e) {
      throw new CsvParsingException("Failed to read CSV content", e);
    }

    return new CsvParseOutcome(transactions, errors);
  }

  private void parseRecords(
      CSVParser parser,
      BankProfile profile,
      DateTimeFormatter dateFormatter,
      List<Transaction> transactions,
      List<RowError> errors) {
    Map<String, Integer> duplicateCounters = new HashMap<>();
    List<String> missingColumns = missingColumns(parser.getHeaderNames(), profile);
    if (!missingColumns.isEmpty()) {
      // Sin las columnas obligatorias ninguna fila es válida: un único error de cabecera en vez
      // de repetir el mismo error (con el contenido de la cabecera dentro) en cada fila.
      errors.add(
          new RowError(
              1,
              RowErrorCode.MISSING_COLUMNS,
              "missing required columns: " + String.join(", ", missingColumns)));
      return;
    }
    Iterator<CSVRecord> records = parser.iterator();
    while (true) {
      CSVRecord record;
      try {
        if (!records.hasNext()) {
          break;
        }
        record = records.next();
      } catch (UncheckedIOException | IllegalStateException malformed) {
        // p. ej. una comilla sin cerrar: a partir de aquí el fichero no se puede leer con
        // fiabilidad. Se conservan las filas válidas anteriores y se informa de dónde empieza.
        errors.add(
            new RowError(
                (int) parser.getRecordNumber() + 2,
                RowErrorCode.MALFORMED_CSV,
                malformed.getMessage()));
        return;
      }
      int rowNumber = (int) record.getRecordNumber() + 1;
      try {
        transactions.add(parseRow(record, profile, dateFormatter, duplicateCounters));
      } catch (InvalidRowException rowException) {
        errors.add(new RowError(rowNumber, rowException.code, rowException.getMessage()));
      } catch (RuntimeException unexpected) {
        // p. ej. una fila con menos columnas que la cabecera.
        errors.add(new RowError(rowNumber, RowErrorCode.INVALID_ROW, unexpected.getMessage()));
      }
    }
  }

  private Transaction parseRow(
      CSVRecord record,
      BankProfile profile,
      DateTimeFormatter dateFormatter,
      Map<String, Integer> duplicateCounters) {
    LocalDate date = parseDate(record.get(profile.dateColumn()).trim(), dateFormatter);
    String concept = record.get(profile.conceptColumn()).trim();
    if (concept.isEmpty()) {
      throw new InvalidRowException(RowErrorCode.BLANK_CONCEPT, "concept must not be blank");
    }
    if (concept.length() > MAX_CONCEPT_LENGTH) {
      throw new InvalidRowException(
          RowErrorCode.CONCEPT_TOO_LONG,
          "concept exceeds maximum length of " + MAX_CONCEPT_LENGTH + " characters");
    }
    String rawAmount = record.get(profile.amountColumn()).trim();
    if (rawAmount.isEmpty()) {
      throw new InvalidRowException(RowErrorCode.BLANK_AMOUNT, "amount must not be blank");
    }
    BigDecimal amount = parseAmount(rawAmount, profile.decimalSeparator());
    // Un extracto bancario nunca trae más decimales que la divisa: si los trae, el fichero está mal
    // y redondearlo en silencio cambiaría el importe real del movimiento.
    int fractionDigits = Money.DEFAULT_CURRENCY.getDefaultFractionDigits();
    if (amount.stripTrailingZeros().scale() > fractionDigits) {
      throw new InvalidRowException(
          RowErrorCode.AMOUNT_TOO_MANY_DECIMALS,
          "amount must have at most " + fractionDigits + " decimal places");
    }
    if (amount.precision() - amount.scale() > MAX_AMOUNT_INTEGER_DIGITS) {
      throw new InvalidRowException(
          RowErrorCode.AMOUNT_TOO_LARGE,
          "amount exceeds maximum of " + MAX_AMOUNT_INTEGER_DIGITS + " integer digits");
    }

    String duplicateKey = date + "|" + concept + "|" + amount.toPlainString();
    int duplicateIndexInFile = duplicateCounters.merge(duplicateKey, 1, Integer::sum) - 1;
    String importHash = ImportHashCalculator.calculate(date, concept, amount, duplicateIndexInFile);

    return new Transaction(
        UUID.randomUUID(),
        date,
        concept,
        Money.of(amount, Money.DEFAULT_CURRENCY),
        null,
        importHash,
        clock.instant());
  }

  // Excel y muchos bancos exportan "UTF-8 con BOM": sin quitarlo, la primera cabecera se lee como
  // "<BOM>fecha" y todas las filas fallan con "Mapping for fecha not found".
  private static Reader skipByteOrderMark(Reader source) throws IOException {
    BufferedReader reader = new BufferedReader(source);
    reader.mark(1);
    if (reader.read() != BYTE_ORDER_MARK) {
      reader.reset();
    }
    return reader;
  }

  private static List<String> missingColumns(List<String> headerNames, BankProfile profile) {
    return Stream.of(profile.dateColumn(), profile.conceptColumn(), profile.amountColumn())
        .filter(column -> !headerNames.contains(column))
        .toList();
  }

  private static LocalDate parseDate(String raw, DateTimeFormatter dateFormatter) {
    try {
      return LocalDate.parse(raw, dateFormatter);
    } catch (DateTimeParseException e) {
      throw new InvalidRowException(RowErrorCode.INVALID_DATE, e.getMessage());
    }
  }

  private static BigDecimal parseAmount(String raw, char decimalSeparator) {
    String normalized = decimalSeparator == ',' ? raw.replace(".", "").replace(',', '.') : raw;
    try {
      return new BigDecimal(normalized);
    } catch (NumberFormatException e) {
      throw new InvalidRowException(RowErrorCode.INVALID_AMOUNT, "invalid amount: " + raw);
    }
  }

  /** Error de una fila con su código; solo vive dentro del parseo de una fila. */
  private static final class InvalidRowException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final RowErrorCode code;

    InvalidRowException(RowErrorCode code, String message) {
      super(message);
      this.code = code;
    }
  }
}
