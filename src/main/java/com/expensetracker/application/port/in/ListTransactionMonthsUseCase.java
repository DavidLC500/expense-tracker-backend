package com.expensetracker.application.port.in;

import java.util.List;

/** Caso de uso: listar los meses que tienen movimientos, para poder filtrar por cualquiera. */
public interface ListTransactionMonthsUseCase {
  List<TransactionMonth> execute();
}
