package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Transaction;
import java.util.List;

/**
 * Caso de uso: importar un lote de movimientos bancarios. Ignora los duplicados por {@code
 * importHash} y categoriza automáticamente los nuevos aplicando las reglas existentes.
 */
public interface ImportTransactionsUseCase {

  ImportSummary execute(List<Transaction> transactions);
}
