package com.expensetracker.application.exception;

import java.io.Serial;
import java.util.UUID;

/** Se lanza cuando un caso de uso busca una {@code Category} por id y no existe. */
public class CategoryNotFoundException extends NotFoundException {

  @Serial private static final long serialVersionUID = 1L;

  public CategoryNotFoundException(UUID id) {
    super("Category", id);
  }
}
