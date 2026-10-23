package com.expensetracker.application.port.in;

/**
 * Caso de uso: reaplicar las reglas de categorización vigentes a todos los movimientos existentes.
 */
public interface RecategorizeTransactionsUseCase {

  /**
   * @return cuántas transacciones cambiaron de categoría.
   */
  int execute();
}
