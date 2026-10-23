package com.expensetracker.domain.exception;

import java.io.Serial;
import java.util.Currency;

/** Se lanza al operar entre dos {@code Money} con divisas distintas. */
public class CurrencyMismatchException extends DomainValidationException {

  @Serial private static final long serialVersionUID = 1L;

  public CurrencyMismatchException(Currency expected, Currency actual) {
    super("Currency mismatch: expected %s but was %s".formatted(expected, actual));
  }
}
