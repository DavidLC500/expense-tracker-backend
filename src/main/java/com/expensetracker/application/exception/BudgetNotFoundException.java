package com.expensetracker.application.exception;

import java.io.Serial;
import java.time.YearMonth;
import java.util.UUID;

/** Se lanza cuando una categoría no tiene presupuesto en el mes indicado. */
public class BudgetNotFoundException extends NotFoundException {

  @Serial private static final long serialVersionUID = 1L;

  public BudgetNotFoundException(UUID categoryId, YearMonth month) {
    super("Budget not found for category " + categoryId + " in " + month);
  }
}
