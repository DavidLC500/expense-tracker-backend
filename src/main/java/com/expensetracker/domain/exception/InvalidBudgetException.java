package com.expensetracker.domain.exception;

import java.io.Serial;

/** Se lanza cuando el importe de un {@code Budget} es negativo. */
public class InvalidBudgetException extends DomainValidationException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidBudgetException(String message) {
    super(message);
  }
}
