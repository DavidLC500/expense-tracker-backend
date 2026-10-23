package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.BudgetStatus;
import java.time.YearMonth;
import java.util.List;

/** Caso de uso: consultar el estado de los presupuestos de un mes frente al gasto real. */
public interface GetBudgetStatusUseCase {

  List<BudgetStatus> execute(YearMonth month);
}
