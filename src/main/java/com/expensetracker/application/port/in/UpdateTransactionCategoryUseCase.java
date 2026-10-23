package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Transaction;
import java.util.UUID;

/** Caso de uso: corregir manualmente la categoría de un movimiento existente. */
public interface UpdateTransactionCategoryUseCase {

  Transaction execute(UUID transactionId, UUID newCategoryId);
}
