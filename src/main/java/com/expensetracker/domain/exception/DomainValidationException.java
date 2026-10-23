package com.expensetracker.domain.exception;

import java.io.Serial;

/**
 * Base de las excepciones que señalan un objeto de dominio inválido o una operación que viola una
 * regla del dominio. El adaptador web las traduce todas a 400 sin conocer cada subclase.
 */
public abstract class DomainValidationException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  protected DomainValidationException(String message) {
    super(message);
  }
}
