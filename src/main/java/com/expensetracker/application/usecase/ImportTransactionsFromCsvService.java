package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.CsvImportSummary;
import com.expensetracker.application.port.in.ImportSummary;
import com.expensetracker.application.port.in.ImportTransactionsFromCsvUseCase;
import com.expensetracker.application.port.in.ImportTransactionsUseCase;
import com.expensetracker.application.port.out.BankProfile;
import com.expensetracker.application.port.out.CsvParseOutcome;
import com.expensetracker.application.port.out.CsvTransactionParser;
import java.io.InputStream;
import java.util.Objects;

/**
 * Orquesta el parseo de CSV y la importación ya existente ({@link ImportTransactionsUseCase}): el
 * parser solo convierte filas en {@code Transaction}/errores, la categorización y la idempotencia
 * por hash siguen viviendo únicamente en el caso de uso de la Fase 2, sin duplicarlas aquí.
 */
public final class ImportTransactionsFromCsvService implements ImportTransactionsFromCsvUseCase {

  private final CsvTransactionParser csvTransactionParser;
  private final ImportTransactionsUseCase importTransactionsUseCase;

  public ImportTransactionsFromCsvService(
      CsvTransactionParser csvTransactionParser,
      ImportTransactionsUseCase importTransactionsUseCase) {
    this.csvTransactionParser =
        Objects.requireNonNull(csvTransactionParser, "csvTransactionParser must not be null");
    this.importTransactionsUseCase =
        Objects.requireNonNull(
            importTransactionsUseCase, "importTransactionsUseCase must not be null");
  }

  @Override
  public CsvImportSummary execute(InputStream csvContent, BankProfile profile) {
    CsvParseOutcome outcome = csvTransactionParser.parse(csvContent, profile);
    ImportSummary summary = importTransactionsUseCase.execute(outcome.transactions());
    return new CsvImportSummary(summary.imported(), summary.duplicatesIgnored(), outcome.errors());
  }
}
