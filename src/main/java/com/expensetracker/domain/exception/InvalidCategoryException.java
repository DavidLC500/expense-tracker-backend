package com.expensetracker.domain.exception;

import java.io.Serial;

/** Se lanza cuando el nombre de una {@code Category} es nulo o está en blanco. */
public class InvalidCategoryException extends DomainValidationException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidCategoryException(String message) {
    super(message);
  }
}
