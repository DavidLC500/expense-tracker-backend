package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Transaction;
import java.util.UUID;

/**
 * Caso de uso: deshacer la corrección manual de un movimiento, para que su categoría la vuelvan a
 * decidir las reglas.
 */
public interface ResetTransactionCategoryUseCase {
  Transaction execute(UUID transactionId);
}
