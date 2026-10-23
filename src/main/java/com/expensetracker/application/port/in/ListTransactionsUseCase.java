package com.expensetracker.application.port.in;

/** Caso de uso: listar movimientos con filtros y paginación. */
public interface ListTransactionsUseCase {

  TransactionPage execute(TransactionFilter filter);
}
