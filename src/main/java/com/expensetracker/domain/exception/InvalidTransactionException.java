package com.expensetracker.domain.exception;

import java.io.Serial;

/** Se lanza cuando un {@code Transaction} se construye con datos que violan sus invariantes. */
public class InvalidTransactionException extends DomainValidationException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidTransactionException(String message) {
    super(message);
  }
}
