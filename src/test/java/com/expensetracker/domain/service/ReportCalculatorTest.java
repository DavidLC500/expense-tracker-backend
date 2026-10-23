package com.expensetracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.expensetracker.domain.model.CategoryExpense;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.MonthlyReport;
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

class ReportCalculatorTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final YearMonth MARCH_2026 = YearMonth.of(2026, 3);

  @Test
  void shouldSumIncomeAndExpenseSeparately() {
    List<Transaction> transactions =
        List.of(
            Transactions.transaction(MARCH_2026.atDay(1), "1500.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(2), "-300.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(3), "-45.50", UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, transactions, List.of(), EUR);

    assertThat(report.totalIncome()).isEqualTo(Money.of(new BigDecimal("1500.00"), EUR));
    assertThat(report.totalExpense()).isEqualTo(Money.of(new BigDecimal("345.50"), EUR));
  }

  @Test
  void shouldCalculateBalanceAsIncomeMinusExpense() {
    List<Transaction> transactions =
        List.of(
            Transactions.transaction(MARCH_2026.atDay(1), "1000.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(2), "-400.00", UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, transactions, List.of(), EUR);

    assertThat(report.balance()).isEqualTo(Money.of(new BigDecimal("600.00"), EUR));
  }

  @Test
  void shouldReturnEmptyReportWhenThereAreNoTransactionsThisMonth() {
    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, List.of(), List.of(), EUR);

    assertThat(report.totalIncome()).isEqualTo(Money.zero(EUR));
    assertThat(report.totalExpense()).isEqualTo(Money.zero(EUR));
    assertThat(report.balance()).isEqualTo(Money.zero(EUR));
    assertThat(report.expensesByCategory()).isEmpty();
    assertThat(report.top5Expenses()).isEmpty();
    assertThat(report.previousMonthExpenseDifference()).isEqualTo(Money.zero(EUR));
  }

  @Test
  void shouldGroupExpensesByCategoryIncludingUncategorizedTransactions() {
    UUID foodCategory = UUID.randomUUID();
    List<Transaction> transactions =
        List.of(
            Transactions.transaction(MARCH_2026.atDay(1), "-20.00", foodCategory),
            Transactions.transaction(MARCH_2026.atDay(2), "-30.00", foodCategory),
            Transactions.transaction(MARCH_2026.atDay(3), "-15.00", null));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, transactions, List.of(), EUR);

    assertThat(report.expensesByCategory())
        .extracting(CategoryExpense::categoryId, CategoryExpense::total)
        .containsExactlyInAnyOrder(
            tuple(foodCategory, Money.of(new BigDecimal("50.00"), EUR)),
            tuple(null, Money.of(new BigDecimal("15.00"), EUR)));
  }

  @Test
  void shouldOrderCategoryExpensesByTotalDescending() {
    UUID smallCategory = UUID.randomUUID();
    UUID bigCategory = UUID.randomUUID();
    List<Transaction> transactions =
        List.of(
            Transactions.transaction(MARCH_2026.atDay(1), "-10.00", smallCategory),
            Transactions.transaction(MARCH_2026.atDay(2), "-90.00", bigCategory));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, transactions, List.of(), EUR);

    assertThat(report.expensesByCategory())
        .extracting(CategoryExpense::categoryId)
        .containsExactly(bigCategory, smallCategory);
  }

  @Test
  void shouldReturnTop5ExpensesOrderedFromHighestToLowest() {
    List<Transaction> transactions =
        List.of(
            Transactions.transaction(MARCH_2026.atDay(1), "-10.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(2), "-500.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(3), "-250.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(4), "-75.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(5), "-120.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(6), "-5.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(7), "2000.00", UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, transactions, List.of(), EUR);

    assertThat(report.top5Expenses())
        .extracting(Transaction::amount)
        .containsExactly(
            Money.of(new BigDecimal("-500.00"), EUR),
            Money.of(new BigDecimal("-250.00"), EUR),
            Money.of(new BigDecimal("-120.00"), EUR),
            Money.of(new BigDecimal("-75.00"), EUR),
            Money.of(new BigDecimal("-10.00"), EUR));
  }

  @Test
  void shouldReturnFewerThanFiveExpensesWhenLessThanFiveExist() {
    List<Transaction> transactions =
        List.of(
            Transactions.transaction(MARCH_2026.atDay(1), "-10.00", UUID.randomUUID()),
            Transactions.transaction(MARCH_2026.atDay(2), "-20.00", UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, transactions, List.of(), EUR);

    assertThat(report.top5Expenses()).hasSize(2);
  }

  @Test
  void shouldComputePositivePreviousMonthExpenseDifferenceWhenSpentMoreThisMonth() {
    List<Transaction> thisMonth =
        List.of(Transactions.transaction(MARCH_2026.atDay(1), "-300.00", UUID.randomUUID()));
    List<Transaction> previousMonth =
        List.of(
            Transactions.transaction(YearMonth.of(2026, 2).atDay(1), "-200.00", UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, thisMonth, previousMonth, EUR);

    assertThat(report.previousMonthExpenseDifference())
        .isEqualTo(Money.of(new BigDecimal("100.00"), EUR));
  }

  @Test
  void shouldComputeNegativePreviousMonthExpenseDifferenceWhenSpentLessThisMonth() {
    List<Transaction> thisMonth =
        List.of(Transactions.transaction(MARCH_2026.atDay(1), "-100.00", UUID.randomUUID()));
    List<Transaction> previousMonth =
        List.of(
            Transactions.transaction(YearMonth.of(2026, 2).atDay(1), "-200.00", UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, thisMonth, previousMonth, EUR);

    assertThat(report.previousMonthExpenseDifference().isNegative()).isTrue();
    assertThat(report.previousMonthExpenseDifference())
        .isEqualTo(Money.of(new BigDecimal("-100.00"), EUR));
  }

  @Test
  void shouldTreatMissingPreviousMonthDataAsZeroExpense() {
    List<Transaction> thisMonth =
        List.of(Transactions.transaction(MARCH_2026.atDay(1), "-300.00", UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, thisMonth, List.of(), EUR);

    assertThat(report.previousMonthExpenseDifference()).isEqualTo(report.totalExpense());
  }

  @ParameterizedTest(name = "income={0} expense={1} -> balance={2}")
  @CsvSource({
    "1000.00, 400.00, 600.00",
    "0.00, 0.00, 0.00",
    "500.00, 500.00, 0.00",
    "100.00, 250.00, -150.00"
  })
  void shouldCalculateBalanceForDifferentIncomeAndExpenseCombinations(
      BigDecimal income, BigDecimal expense, BigDecimal expectedBalance) {
    List<Transaction> transactions =
        List.of(
            Transactions.transaction(
                MARCH_2026.atDay(1), income.toPlainString(), UUID.randomUUID()),
            Transactions.transaction(
                MARCH_2026.atDay(2), expense.negate().toPlainString(), UUID.randomUUID()));

    MonthlyReport report = ReportCalculator.calculate(MARCH_2026, transactions, List.of(), EUR);

    assertThat(report.balance()).isEqualTo(Money.of(expectedBalance, EUR));
  }
}
