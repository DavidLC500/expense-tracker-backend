package com.expensetracker.application.port.in;

import java.time.YearMonth;
import java.util.UUID;

/** Caso de uso: quitar el presupuesto de una categoría en un mes. */
public interface DeleteBudgetUseCase {

  void execute(YearMonth month, UUID categoryId);
}
