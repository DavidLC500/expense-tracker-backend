package com.expensetracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.BudgetStatus;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.testdata.Transactions;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BudgetCalculatorTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final YearMonth MARCH_2026 = YearMonth.of(2026, 3);

  @Test
  void shouldReturnZeroSpentAndNotExceededWhenCategoryHasNoTransactionsThisMonth() {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.of(new BigDecimal("200.00"), EUR));

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of());

    assertThat(statuses)
        .singleElement()
        .satisfies(
            status -> {
              assertThat(status.spent()).isEqualTo(Money.zero(EUR));
              assertThat(status.remaining()).isEqualTo(Money.of(new BigDecimal("200.00"), EUR));
              assertThat(status.exceeded()).isFalse();
            });
  }

  @Test
  void shouldMarkBudgetAsExceededWhenSpentIsGreaterThanBudgeted() {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.of(new BigDecimal("100.00"), EUR));
    Transaction expense = Transactions.expense(MARCH_2026.atDay(10), "150.00", categoryId);

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of(expense));

    assertThat(statuses)
        .singleElement()
        .satisfies(
            status -> {
              assertThat(status.exceeded()).isTrue();
              assertThat(status.remaining().isNegative()).isTrue();
            });
  }

  @Test
  void shouldNotMarkBudgetAsExceededWhenSpentEqualsBudgeted() {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.of(new BigDecimal("100.00"), EUR));
    Transaction expense = Transactions.expense(MARCH_2026.atDay(10), "100.00", categoryId);

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of(expense));

    assertThat(statuses)
        .singleElement()
        .satisfies(
            status -> {
              assertThat(status.exceeded()).isFalse();
              assertThat(status.remaining()).isEqualTo(Money.zero(EUR));
            });
  }

  @Test
  void shouldIgnoreIncomeTransactionsWhenCalculatingSpent() {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.of(new BigDecimal("100.00"), EUR));
    Transaction refund = Transactions.income(MARCH_2026.atDay(5), "50.00", categoryId);

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of(refund));

    assertThat(statuses).singleElement().extracting(BudgetStatus::spent).isEqualTo(Money.zero(EUR));
  }

  @Test
  void shouldIgnoreTransactionsFromOtherCategories() {
    UUID categoryId = UUID.randomUUID();
    UUID otherCategoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.of(new BigDecimal("100.00"), EUR));
    Transaction otherCategoryExpense =
        Transactions.expense(MARCH_2026.atDay(5), "80.00", otherCategoryId);

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of(otherCategoryExpense));

    assertThat(statuses).singleElement().extracting(BudgetStatus::spent).isEqualTo(Money.zero(EUR));
  }

  @Test
  void shouldIgnoreTransactionsFromOtherMonths() {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.of(new BigDecimal("100.00"), EUR));
    Transaction aprilExpense =
        Transactions.expense(YearMonth.of(2026, 4).atDay(1), "80.00", categoryId);

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of(aprilExpense));

    assertThat(statuses).singleElement().extracting(BudgetStatus::spent).isEqualTo(Money.zero(EUR));
  }

  @Test
  void shouldIgnoreBudgetsNotMatchingRequestedMonth() {
    UUID categoryId = UUID.randomUUID();
    Budget aprilBudget =
        Budget.of(categoryId, YearMonth.of(2026, 4), Money.of(new BigDecimal("100.00"), EUR));

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(aprilBudget), List.of());

    assertThat(statuses).isEmpty();
  }

  @Test
  void shouldReturnEmptyListWhenThereAreNoBudgetsForTheMonth() {
    List<BudgetStatus> statuses = BudgetCalculator.calculate(MARCH_2026, List.of(), List.of());

    assertThat(statuses).isEmpty();
  }

  @Test
  void shouldReturnZeroPercentageWhenBudgetedIsZeroAndNothingWasSpent() {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.zero(EUR));

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of());

    assertThat(statuses)
        .singleElement()
        .satisfies(
            status -> {
              assertThat(status.percentageUsed()).isEqualByComparingTo(BigDecimal.ZERO);
              assertThat(status.exceeded()).isFalse();
            });
  }

  @Test
  void shouldCapPercentageAtOneHundredWhenBudgetedIsZeroAndSomethingWasSpent() {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.zero(EUR));
    Transaction expense = Transactions.expense(MARCH_2026.atDay(3), "20.00", categoryId);

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of(expense));

    assertThat(statuses)
        .singleElement()
        .satisfies(
            status -> {
              assertThat(status.percentageUsed()).isEqualByComparingTo(new BigDecimal("100"));
              assertThat(status.exceeded()).isTrue();
            });
  }

  @ParameterizedTest(name = "spent={0} budgeted={1} -> percentageUsed={2}")
  @CsvSource({
    "50.00, 200.00, 25.00",
    "100.00, 100.00, 100.00",
    "150.00, 100.00, 150.00",
    "1.00, 3.00, 33.33",
    "33.33, 100.00, 33.33"
  })
  void shouldCalculatePercentageUsedWithTwoDecimalsHalfEvenRounding(
      BigDecimal spent, BigDecimal budgeted, BigDecimal expectedPercentage) {
    UUID categoryId = UUID.randomUUID();
    Budget budget = Budget.of(categoryId, MARCH_2026, Money.of(budgeted, EUR));
    Transaction expense =
        Transactions.expense(MARCH_2026.atDay(1), spent.toPlainString(), categoryId);

    List<BudgetStatus> statuses =
        BudgetCalculator.calculate(MARCH_2026, List.of(budget), List.of(expense));

    assertThat(statuses)
        .singleElement()
        .extracting(BudgetStatus::percentageUsed)
        .satisfies(percentage -> assertThat(percentage).isEqualByComparingTo(expectedPercentage));
  }
}
