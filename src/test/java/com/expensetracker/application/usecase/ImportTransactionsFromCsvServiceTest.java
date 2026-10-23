package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.application.port.in.CsvImportSummary;
import com.expensetracker.application.port.in.ImportTransactionsUseCase;
import com.expensetracker.application.port.out.BankProfile;
import com.expensetracker.application.port.out.CsvParseOutcome;
import com.expensetracker.application.port.out.CsvTransactionParser;
import com.expensetracker.application.port.out.RowError;
import com.expensetracker.application.port.out.RowErrorCode;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.testdata.InMemoryRuleRepository;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportTransactionsFromCsvServiceTest {

  private static final UUID FOOD_CATEGORY = UUID.randomUUID();

  private InMemoryTransactionRepository transactionRepository;
  private InMemoryRuleRepository ruleRepository;
  private ImportTransactionsFromCsvService service;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    ruleRepository = new InMemoryRuleRepository();
    ImportTransactionsUseCase importTransactionsUseCase =
        new ImportTransactionsService(transactionRepository, ruleRepository);
    service =
        new ImportTransactionsFromCsvService(
            new StubCsvTransactionParser(), importTransactionsUseCase);
  }

  @Test
  void shouldImportParsedTransactionsApplyingExistingCategorizationRules() {
    ruleRepository.save(Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    CsvImportSummary summary = service.execute(emptyStream(), BankProfile.generic());

    assertThat(summary.imported()).isEqualTo(2);
    assertThat(summary.duplicatesIgnored()).isEqualTo(0);
    assertThat(summary.rowErrors()).hasSize(1);
    assertThat(summary.rowErrors().get(0))
        .isEqualTo(new RowError(3, RowErrorCode.INVALID_AMOUNT, "unparseable amount"));
    assertThat(transactionRepository.findAll())
        .filteredOn(t -> t.concept().equals("COMPRA MERCADONA"))
        .singleElement()
        .extracting(t -> t.categoryId())
        .isEqualTo(FOOD_CATEGORY);
  }

  private static InputStream emptyStream() {
    return new ByteArrayInputStream(new byte[0]);
  }

  /**
   * Fake del puerto de parseo: devuelve siempre el mismo resultado fijo, sin leer de verdad el
   * {@code InputStream} — el parseo real de Commons CSV se prueba en la Fase 5, adaptador de
   * infraestructura; aquí solo importa que este servicio orquesta bien parser + caso de uso.
   */
  private static final class StubCsvTransactionParser implements CsvTransactionParser {
    @Override
    public CsvParseOutcome parse(InputStream csvContent, BankProfile profile) {
      return new CsvParseOutcome(
          List.of(
              Transactions.expense(LocalDate.of(2026, 1, 5), "COMPRA MERCADONA", "20.00", null),
              Transactions.expense(LocalDate.of(2026, 1, 6), "GASOLINERA REPSOL", "30.00", null)),
          List.of(new RowError(3, RowErrorCode.INVALID_AMOUNT, "unparseable amount")));
    }
  }
}
