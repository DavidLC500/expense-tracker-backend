package com.expensetracker.application.port.out;

import com.expensetracker.domain.model.Transaction;
import java.util.List;

/** Resultado de parsear un CSV: las filas válidas (como {@link Transaction}) y las que fallaron. */
public record CsvParseOutcome(List<Transaction> transactions, List<RowError> errors) {

  public CsvParseOutcome {
    transactions = List.copyOf(transactions);
    errors = List.copyOf(errors);
  }
}
