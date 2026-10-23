package com.expensetracker.application.exception;

import java.io.Serial;
import java.util.UUID;

/** Se lanza cuando un caso de uso busca una {@code Transaction} por id y no existe. */
public class TransactionNotFoundException extends NotFoundException {

  @Serial private static final long serialVersionUID = 1L;

  public TransactionNotFoundException(UUID id) {
    super("Transaction", id);
  }
}
