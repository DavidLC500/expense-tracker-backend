package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Rule;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.testdata.InMemoryRuleRepository;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecategorizeTransactionsServiceTest {

  private static final LocalDate MARCH_15 = LocalDate.of(2026, 3, 15);
  private static final UUID FOOD_CATEGORY = UUID.randomUUID();
  private static final UUID OTHER_CATEGORY = UUID.randomUUID();

  private InMemoryTransactionRepository transactionRepository;
  private InMemoryRuleRepository ruleRepository;
  private RecategorizeTransactionsService service;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    ruleRepository = new InMemoryRuleRepository();
    service = new RecategorizeTransactionsService(transactionRepository, ruleRepository);
  }

  private static Transaction transactionWithConceptAndCategory(String concept, UUID categoryId) {
    return Transactions.expense(MARCH_15, concept, "10.00", categoryId);
  }

  @Test
  void shouldRecategorizeTransactionWhenNewRuleMatches() {
    Transaction stored =
        transactionRepository.save(
            transactionWithConceptAndCategory("COMPRA MERCADONA MADRID", null));
    ruleRepository.save(Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    int changed = service.execute();

    assertThat(changed).isEqualTo(1);
    assertThat(transactionRepository.findById(stored.id()).orElseThrow().categoryId())
        .isEqualTo(FOOD_CATEGORY);
  }

  @Test
  void shouldNotCountTransactionsWhoseCategoryStaysTheSame() {
    ruleRepository.save(Rule.of("MERCADONA", FOOD_CATEGORY, 1));
    transactionRepository.save(
        transactionWithConceptAndCategory("COMPRA MERCADONA MADRID", FOOD_CATEGORY));

    int changed = service.execute();

    assertThat(changed).isEqualTo(0);
  }

  @Test
  void shouldRecategorizeWhenRuleCategoryChanges() {
    Transaction stored =
        transactionRepository.save(
            transactionWithConceptAndCategory("COMPRA MERCADONA MADRID", FOOD_CATEGORY));
    ruleRepository.save(Rule.of("MERCADONA", OTHER_CATEGORY, 1));

    int changed = service.execute();

    assertThat(changed).isEqualTo(1);
    assertThat(transactionRepository.findById(stored.id()).orElseThrow().categoryId())
        .isEqualTo(OTHER_CATEGORY);
  }

  @Test
  void shouldKeepManualCategoryWhenRulesWouldChangeIt() {
    Transaction manual =
        transactionRepository.save(
            transactionWithConceptAndCategory("COMPRA MERCADONA MADRID", null)
                .withManualCategory(OTHER_CATEGORY));
    ruleRepository.save(Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    int changed = service.execute();

    assertThat(changed).isZero();
    assertThat(transactionRepository.findById(manual.id()).orElseThrow().categoryId())
        .isEqualTo(OTHER_CATEGORY);
  }

  @Test
  void shouldKeepManualCategoryWhenNoRuleMatches() {
    Transaction manual =
        transactionRepository.save(
            transactionWithConceptAndCategory("CAFE CENTRAL", null)
                .withManualCategory(FOOD_CATEGORY));

    int changed = service.execute();

    assertThat(changed).isZero();
    assertThat(transactionRepository.findById(manual.id()).orElseThrow().categoryId())
        .isEqualTo(FOOD_CATEGORY);
  }

  @Test
  void shouldUncategorizeWhenNoRuleMatchesAnymore() {
    Transaction stored =
        transactionRepository.save(
            transactionWithConceptAndCategory("COMPRA MERCADONA MADRID", FOOD_CATEGORY));

    int changed = service.execute();

    assertThat(changed).isEqualTo(1);
    assertThat(transactionRepository.findById(stored.id()).orElseThrow().categoryId()).isNull();
  }
}
