package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Transaction;
import java.util.List;

/** Página de resultados de {@code TransactionRepository.findByFilter}. */
public record TransactionPage(List<Transaction> items, int page, int pageSize, long totalElements) {

  public TransactionPage {
    items = List.copyOf(items);
  }
}
