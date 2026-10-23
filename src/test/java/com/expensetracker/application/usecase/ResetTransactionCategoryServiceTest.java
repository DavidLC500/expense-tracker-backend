package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.TransactionNotFoundException;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.testdata.InMemoryRuleRepository;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResetTransactionCategoryServiceTest {

  private static final LocalDate MARCH_15 = LocalDate.of(2026, 3, 15);
  private static final UUID FOOD_CATEGORY = UUID.randomUUID();
  private static final UUID MANUAL_CATEGORY = UUID.randomUUID();

  private InMemoryTransactionRepository transactionRepository;
  private InMemoryRuleRepository ruleRepository;
  private ResetTransactionCategoryService service;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    ruleRepository = new InMemoryRuleRepository();
    service = new ResetTransactionCategoryService(transactionRepository, ruleRepository);
  }

  private Transaction storedManual(String concept) {
    return transactionRepository.save(
        Transactions.expense(MARCH_15, concept, "10.00", null).withManualCategory(MANUAL_CATEGORY));
  }

  @Test
  void shouldHandAManualCorrectionBackToTheRules() {
    Transaction manual = storedManual("COMPRA MERCADONA MADRID");
    ruleRepository.save(Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    Transaction reset = service.execute(manual.id());

    assertThat(reset.categoryId()).isEqualTo(FOOD_CATEGORY);
    assertThat(reset.manuallyCategorized()).isFalse();
    assertThat(transactionRepository.findById(manual.id())).contains(reset);
  }

  @Test
  void shouldLeaveItUncategorizedWhenNoRuleMatches() {
    Transaction manual = storedManual("CAFE CENTRAL");

    Transaction reset = service.execute(manual.id());

    assertThat(reset.categoryId()).isNull();
    assertThat(reset.manuallyCategorized()).isFalse();
  }

  @Test
  void shouldThrowWhenTheTransactionDoesNotExist() {
    UUID unknown = UUID.randomUUID();

    assertThatThrownBy(() -> service.execute(unknown))
        .isInstanceOf(TransactionNotFoundException.class);
  }
}
