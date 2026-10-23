package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.application.port.in.ImportSummary;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.testdata.InMemoryRuleRepository;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportTransactionsServiceTest {

  private static final LocalDate MARCH_15 = LocalDate.of(2026, 3, 15);
  private static final UUID FOOD_CATEGORY = UUID.randomUUID();

  private InMemoryTransactionRepository transactionRepository;
  private InMemoryRuleRepository ruleRepository;
  private ImportTransactionsService service;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    ruleRepository = new InMemoryRuleRepository();
    service = new ImportTransactionsService(transactionRepository, ruleRepository);
  }

  private static Transaction transactionWithConcept(String concept) {
    return Transactions.expense(MARCH_15, concept, "10.00", null);
  }

  @Test
  void shouldImportNewTransactions() {
    Transaction transaction = transactionWithConcept("COMPRA VARIOS");

    ImportSummary summary = service.execute(List.of(transaction));

    assertThat(summary.imported()).isEqualTo(1);
    assertThat(summary.duplicatesIgnored()).isEqualTo(0);
    assertThat(transactionRepository.findAll())
        .extracting(Transaction::id)
        .containsExactly(transaction.id());
  }

  @Test
  void shouldIgnoreDuplicateTransactionsOnReimport() {
    Transaction transaction = transactionWithConcept("COMPRA VARIOS");
    service.execute(List.of(transaction));

    ImportSummary summary = service.execute(List.of(transaction));

    assertThat(summary.imported()).isEqualTo(0);
    assertThat(summary.duplicatesIgnored()).isEqualTo(1);
    assertThat(transactionRepository.findAll()).hasSize(1);
  }

  @Test
  void shouldAutoCategorizeApplyingExistingRules() {
    ruleRepository.save(Rule.of("MERCADONA", FOOD_CATEGORY, 1));
    Transaction transaction = transactionWithConcept("COMPRA MERCADONA MADRID");

    service.execute(List.of(transaction));

    Transaction saved = transactionRepository.findById(transaction.id()).orElseThrow();
    assertThat(saved.categoryId()).isEqualTo(FOOD_CATEGORY);
  }

  @Test
  void shouldOverwriteIncomingCategoryIdWithCategorizerResult() {
    ruleRepository.save(Rule.of("MERCADONA", FOOD_CATEGORY, 1));
    Transaction incoming =
        transactionWithConcept("COMPRA MERCADONA MADRID").withRuleCategory(UUID.randomUUID());

    service.execute(List.of(incoming));

    Transaction saved = transactionRepository.findById(incoming.id()).orElseThrow();
    assertThat(saved.categoryId()).isEqualTo(FOOD_CATEGORY);
  }

  @Test
  void shouldLeaveUncategorizedWhenNoRuleMatches() {
    ruleRepository.save(Rule.of("NETFLIX", UUID.randomUUID(), 1));
    Transaction transaction = transactionWithConcept("GASOLINERA REPSOL");

    service.execute(List.of(transaction));

    Transaction saved = transactionRepository.findById(transaction.id()).orElseThrow();
    assertThat(saved.categoryId()).isNull();
  }

  @Test
  void shouldCountOnlyRowsTheRepositoryActuallyInsertedAndTheRestAsDuplicates() {
    Transaction alreadyImported = transactionWithConcept("COMPRA VARIOS");
    service.execute(List.of(alreadyImported));
    Transaction newOne = transactionWithConcept("OTRA COMPRA");

    ImportSummary summary = service.execute(List.of(alreadyImported, newOne));

    assertThat(summary.imported()).isEqualTo(1);
    assertThat(summary.duplicatesIgnored()).isEqualTo(1);
    assertThat(transactionRepository.findAll())
        .extracting(Transaction::id)
        .containsExactlyInAnyOrder(alreadyImported.id(), newOne.id());
  }
}
