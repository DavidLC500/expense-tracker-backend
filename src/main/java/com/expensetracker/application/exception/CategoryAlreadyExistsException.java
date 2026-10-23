package com.expensetracker.application.exception;

import java.io.Serial;

/**
 * Se lanza al crear una {@code Category} cuyo nombre ya existe, sin distinguir mayúsculas ni
 * tildes.
 */
public class CategoryAlreadyExistsException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public CategoryAlreadyExistsException(String message) {
    super(message);
  }
}
