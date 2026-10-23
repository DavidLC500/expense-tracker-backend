package com.expensetracker.domain.service;

import com.expensetracker.domain.model.CategoryExpense;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.MonthlyReport;
import com.expensetracker.domain.model.Transaction;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Calcula el informe mensual de ingresos, gastos y comparación con el mes anterior. */
public final class ReportCalculator {

  private static final int TOP_EXPENSES_LIMIT = 5;

  private ReportCalculator() {}

  public static MonthlyReport calculate(
      YearMonth month,
      List<Transaction> transactionsThisMonth,
      List<Transaction> transactionsPreviousMonth,
      Currency currency) {
    Money totalIncome = sumPositive(transactionsThisMonth, currency);
    Money totalExpense = sumNegativeAbsolute(transactionsThisMonth, currency);
    Money balance = totalIncome.subtract(totalExpense);
    List<CategoryExpense> expensesByCategory =
        groupExpensesByCategory(transactionsThisMonth, currency);
    List<Transaction> top5Expenses = top5Expenses(transactionsThisMonth);
    Money previousMonthExpense = sumNegativeAbsolute(transactionsPreviousMonth, currency);
    Money previousMonthExpenseDifference = totalExpense.subtract(previousMonthExpense);

    return new MonthlyReport(
        month,
        totalIncome,
        totalExpense,
        balance,
        expensesByCategory,
        top5Expenses,
        previousMonthExpenseDifference);
  }

  private static Money sumPositive(List<Transaction> transactions, Currency currency) {
    return transactions.stream()
        .map(Transaction::amount)
        .filter(Money::isPositive)
        .reduce(Money.zero(currency), Money::add);
  }

  private static Money sumNegativeAbsolute(List<Transaction> transactions, Currency currency) {
    return transactions.stream()
        .map(Transaction::amount)
        .filter(Money::isNegative)
        .map(Money::negate)
        .reduce(Money.zero(currency), Money::add);
  }

  // Las transacciones sin categoría (categoryId == null, ver Transaction#isUncategorized) se
  // agrupan igualmente bajo la clave null: siguen siendo gasto real del mes y deben aparecer en
  // el desglose por categoría (como "Sin categoría"), no se descartan silenciosamente.
  private static List<CategoryExpense> groupExpensesByCategory(
      List<Transaction> transactions, Currency currency) {
    Map<UUID, Money> totalsByCategory = new LinkedHashMap<>();
    for (Transaction transaction : transactions) {
      if (!transaction.amount().isNegative()) {
        continue;
      }
      UUID categoryId = transaction.categoryId();
      Money current = totalsByCategory.getOrDefault(categoryId, Money.zero(currency));
      totalsByCategory.put(categoryId, current.add(transaction.amount().negate()));
    }
    return totalsByCategory.entrySet().stream()
        .map(entry -> new CategoryExpense(entry.getKey(), entry.getValue()))
        .sorted(
            Comparator.comparing((CategoryExpense expense) -> expense.total().amount()).reversed())
        .toList();
  }

  private static List<Transaction> top5Expenses(List<Transaction> transactions) {
    return transactions.stream()
        .filter(transaction -> transaction.amount().isNegative())
        .sorted(Comparator.comparing(transaction -> transaction.amount().amount()))
        .limit(TOP_EXPENSES_LIMIT)
        .toList();
  }
}
