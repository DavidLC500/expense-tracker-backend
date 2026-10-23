package com.expensetracker.infrastructure.adapter.out.csv;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.application.port.out.BankProfile;
import com.expensetracker.application.port.out.CsvParseOutcome;
import com.expensetracker.application.port.out.RowError;
import com.expensetracker.application.port.out.RowErrorCode;
import com.expensetracker.domain.model.Transaction;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CsvTransactionParserAdapterTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneOffset.UTC);

  private final CsvTransactionParserAdapter parser = new CsvTransactionParserAdapter(FIXED_CLOCK);

  @Test
  void shouldParseValidRowsWithGenericProfile() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,MERCADONA MADRID,-45.30
        2026-01-07,NOMINA EMPRESA SL,1800.00
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.errors()).isEmpty();
    assertThat(outcome.transactions()).hasSize(2);
    Transaction first = outcome.transactions().get(0);
    assertThat(first.date()).isEqualTo(LocalDate.of(2026, 1, 5));
    assertThat(first.concept()).isEqualTo("MERCADONA MADRID");
    assertThat(first.amount().amount()).isEqualByComparingTo("-45.30");
    assertThat(first.categoryId()).isNull();
    assertThat(first.createdAt()).isEqualTo(FIXED_CLOCK.instant());
  }

  @Test
  void shouldRecordRowErrorForInvalidDateWithoutStoppingOtherRows() {
    String csv =
        """
        fecha,concepto,importe
        not-a-date,MERCADONA MADRID,-45.30
        2026-01-07,NOMINA EMPRESA SL,1800.00
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).hasSize(1);
    assertThat(outcome.errors()).hasSize(1);
    assertThat(outcome.errors().get(0).rowNumber()).isEqualTo(2);
    assertThat(outcome.errors().get(0).code()).isEqualTo(RowErrorCode.INVALID_DATE);
  }

  @Test
  void shouldRecordRowErrorForConceptExceedingColumnLengthWithoutFailingOtherRows() {
    String tooLongConcept = "X".repeat(501);
    String csv =
        "fecha,concepto,importe\n"
            + "2026-10-01,"
            + tooLongConcept
            + ",-10.00\n"
            + "2026-10-02,CONCEPTO NORMAL,-5.00\n";

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).hasSize(1);
    assertThat(outcome.transactions().get(0).concept()).isEqualTo("CONCEPTO NORMAL");
    assertThat(outcome.errors()).hasSize(1);
    assertThat(outcome.errors().get(0).rowNumber()).isEqualTo(2);
    assertThat(outcome.errors().get(0).code()).isEqualTo(RowErrorCode.CONCEPT_TOO_LONG);
    assertThat(outcome.errors().get(0).reason()).contains("500");
  }

  @Test
  void shouldRecordRowErrorForUnparseableAmount() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,MERCADONA MADRID,not-a-number
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).isEmpty();
    assertThat(outcome.errors()).hasSize(1);
    assertThat(outcome.errors().get(0).rowNumber()).isEqualTo(2);
    assertThat(outcome.errors().get(0).code()).isEqualTo(RowErrorCode.INVALID_AMOUNT);
  }

  @Test
  void shouldRecordRowErrorForAmountExceedingColumnPrecisionWithoutFailingOtherRows() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,IMPORTE ENORME,-999999999999999.99
        2026-01-06,TRECE DIGITOS,1000000000000.00
        2026-01-07,LIMITE EXACTO,-999999999999.99
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).hasSize(1);
    assertThat(outcome.transactions().get(0).amount().amount())
        .isEqualByComparingTo("-999999999999.99");
    assertThat(outcome.errors()).extracting(RowError::rowNumber).containsExactly(2, 3);
    assertThat(outcome.errors())
        .extracting(RowError::code)
        .containsOnly(RowErrorCode.AMOUNT_TOO_LARGE);
    assertThat(outcome.errors().get(0).reason()).contains("12 integer digits");
  }

  @Test
  void shouldIgnoreUtf8ByteOrderMarkAtStartOfFile() {
    char byteOrderMark = 0xFEFF;
    String csv =
        byteOrderMark
            + """
            fecha,concepto,importe
            2026-01-05,MERCADONA MADRID,-45.30
            """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.errors()).isEmpty();
    assertThat(outcome.transactions()).hasSize(1);
    assertThat(outcome.transactions().get(0).concept()).isEqualTo("MERCADONA MADRID");
  }

  @Test
  void shouldRecordRowErrorForAmountWithMoreDecimalsThanCurrencyInsteadOfRounding() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,TRES DECIMALES,-1.999
        2026-01-06,CERO SOBRANTE,-1.990
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).hasSize(1);
    assertThat(outcome.transactions().get(0).amount().amount()).isEqualByComparingTo("-1.99");
    assertThat(outcome.errors()).hasSize(1);
    assertThat(outcome.errors().get(0).rowNumber()).isEqualTo(2);
    assertThat(outcome.errors().get(0).code()).isEqualTo(RowErrorCode.AMOUNT_TOO_MANY_DECIMALS);
    assertThat(outcome.errors().get(0).reason()).contains("2 decimal places");
  }

  @Test
  void shouldRecordDescriptiveRowErrorForBlankAmount() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,SIN IMPORTE,
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.errors()).hasSize(1);
    assertThat(outcome.errors().get(0).code()).isEqualTo(RowErrorCode.BLANK_AMOUNT);
  }

  @Test
  void shouldRecordSingleHeaderErrorWhenRequiredColumnsAreMissing() {
    String csv =
        """
        fecha,descripcion
        2026-01-05,MERCADONA MADRID
        2026-01-06,NETFLIX
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).isEmpty();
    assertThat(outcome.errors()).hasSize(1);
    assertThat(outcome.errors().get(0).rowNumber()).isEqualTo(1);
    assertThat(outcome.errors().get(0).code()).isEqualTo(RowErrorCode.MISSING_COLUMNS);
    assertThat(outcome.errors().get(0).reason())
        .isEqualTo("missing required columns: concepto, importe");
  }

  @Test
  void shouldRecordBlankConceptAndShortRowWithTheirCodes() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,,-1.00
        2026-01-06,SOLO DOS COLUMNAS
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).isEmpty();
    assertThat(outcome.errors())
        .extracting(RowError::code)
        .containsExactly(RowErrorCode.BLANK_CONCEPT, RowErrorCode.INVALID_ROW);
  }

  @Test
  void shouldAcceptTrailingCommaThatCreatesAnUnnamedColumn() {
    String csv =
        """
        fecha,concepto,importe,
        2026-01-05,MERCADONA MADRID,-45.30,
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.errors()).isEmpty();
    assertThat(outcome.transactions()).hasSize(1);
  }

  @Test
  void shouldKeepPreviousRowsAndReportMalformedCsvWhenAQuoteIsNeverClosed() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,ANTES,-1.00
        2026-01-06,"SIN CERRAR,-1.00
        2026-01-07,DESPUES,-1.00
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).extracting(Transaction::concept).containsExactly("ANTES");
    assertThat(outcome.errors()).hasSize(1);
    assertThat(outcome.errors().get(0).code()).isEqualTo(RowErrorCode.MALFORMED_CSV);
    assertThat(outcome.errors().get(0).rowNumber()).isEqualTo(3);
  }

  @Test
  void shouldRejectImpossibleDatesInsteadOfAdjustingThem() {
    String csv =
        """
        fecha,concepto,importe
        2026-02-30,TREINTA DE FEBRERO,-1.00
        2026-04-31,TREINTA Y UNO DE ABRIL,-1.00
        2028-02-29,BISIESTO,-1.00
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions())
        .extracting(Transaction::date)
        .containsExactly(LocalDate.of(2028, 2, 29));
    assertThat(outcome.errors())
        .extracting(RowError::code)
        .containsExactly(RowErrorCode.INVALID_DATE, RowErrorCode.INVALID_DATE);
  }

  @Test
  void shouldAssignDifferentHashesToIdenticalRowsInTheSameFile() {
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,CAFETERIA,-1.50
        2026-01-05,CAFETERIA,-1.50
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), BankProfile.generic());

    assertThat(outcome.transactions()).hasSize(2);
    String firstHash = outcome.transactions().get(0).importHash();
    String secondHash = outcome.transactions().get(1).importHash();
    assertThat(firstHash).isNotEqualTo(secondHash);
  }

  @Test
  void shouldParseAmountsWithCommaAsDecimalSeparator() {
    BankProfile commaProfile =
        new BankProfile("comma-bank", "fecha", "concepto", "importe", "uuuu-MM-dd", ',');
    String csv =
        """
        fecha,concepto,importe
        2026-01-05,COMPRA GRANDE,"-1.234,56"
        """;

    CsvParseOutcome outcome = parser.parse(inputStream(csv), commaProfile);

    assertThat(outcome.errors()).isEmpty();
    assertThat(outcome.transactions()).singleElement();
    BigDecimal amount = outcome.transactions().get(0).amount().amount();
    assertThat(amount).isEqualByComparingTo("-1234.56");
  }

  private static InputStream inputStream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }
}
