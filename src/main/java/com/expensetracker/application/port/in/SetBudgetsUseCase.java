package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Budget;
import java.time.YearMonth;
import java.util.List;

/** Caso de uso: definir los presupuestos de un mes para una o varias categorías. */
public interface SetBudgetsUseCase {

  List<Budget> execute(YearMonth month, List<BudgetAmount> budgetAmounts);
}
