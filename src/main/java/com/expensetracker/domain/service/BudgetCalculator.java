package com.expensetracker.domain.service;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.BudgetStatus;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Calcula el estado de los presupuestos de un mes a partir de las transacciones reales. */
public final class BudgetCalculator {

  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  private BudgetCalculator() {}

  public static List<BudgetStatus> calculate(
      YearMonth month, List<Budget> budgets, List<Transaction> transactions) {
    return budgets.stream()
        .filter(budget -> budget.month().equals(month))
        .map(budget -> toStatus(month, budget, transactions))
        .toList();
  }

  private static BudgetStatus toStatus(
      YearMonth month, Budget budget, List<Transaction> transactions) {
    Money spent = sumSpent(month, budget.categoryId(), budget.amount(), transactions);
    Money remaining = budget.amount().subtract(spent);
    BigDecimal percentageUsed = percentageUsed(spent, budget.amount());
    boolean exceeded = remaining.isNegative();
    return new BudgetStatus(
        budget.categoryId(), month, budget.amount(), spent, remaining, percentageUsed, exceeded);
  }

  private static Money sumSpent(
      YearMonth month, UUID categoryId, Money budgeted, List<Transaction> transactions) {
    return transactions.stream()
        .filter(transaction -> YearMonth.from(transaction.date()).equals(month))
        .filter(transaction -> categoryId.equals(transaction.categoryId()))
        .filter(transaction -> transaction.amount().isNegative())
        .map(transaction -> transaction.amount().negate())
        .reduce(Money.zero(budgeted.currency()), Money::add);
  }

  // División por cero: sin presupuesto (0) no hay ratio matemático válido. Si tampoco se gastó
  // nada, 0% es la lectura natural. Si se gastó algo, capamos a 100% para que una barra de
  // progreso tenga sentido; `exceeded` (ya true en este caso vía remaining negativo) es la fuente
  // de verdad real, no este porcentaje.
  private static BigDecimal percentageUsed(Money spent, Money budgeted) {
    if (budgeted.isZero()) {
      return spent.isZero() ? BigDecimal.ZERO : ONE_HUNDRED;
    }
    return spent
        .amount()
        .multiply(ONE_HUNDRED)
        .divide(budgeted.amount(), 2, RoundingMode.HALF_EVEN);
  }
}
