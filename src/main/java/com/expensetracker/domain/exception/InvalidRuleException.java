package com.expensetracker.domain.exception;

import java.io.Serial;

/** Se lanza cuando el patrón de una {@code Rule} es nulo o está en blanco. */
public class InvalidRuleException extends DomainValidationException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidRuleException(String message) {
    super(message);
  }
}
