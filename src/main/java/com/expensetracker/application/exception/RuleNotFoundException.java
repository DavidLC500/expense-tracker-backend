package com.expensetracker.application.exception;

import java.io.Serial;
import java.util.UUID;

/** Se lanza cuando un caso de uso busca una {@code Rule} por id y no existe. */
public class RuleNotFoundException extends NotFoundException {

  @Serial private static final long serialVersionUID = 1L;

  public RuleNotFoundException(UUID id) {
    super("Rule", id);
  }
}
