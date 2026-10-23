package com.expensetracker.domain.model;

import java.time.YearMonth;
import java.util.List;

/** Informe mensual de ingresos, gastos, balance y comparación con el mes anterior. */
public record MonthlyReport(
    YearMonth month,
    Money totalIncome,
    Money totalExpense,
    Money balance,
    List<CategoryExpense> expensesByCategory,
    List<Transaction> top5Expenses,
    Money previousMonthExpenseDifference) {

  public MonthlyReport {
    expensesByCategory = List.copyOf(expensesByCategory);
    top5Expenses = List.copyOf(top5Expenses);
  }
}
