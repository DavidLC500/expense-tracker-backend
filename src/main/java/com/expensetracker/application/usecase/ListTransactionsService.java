package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.ListTransactionsUseCase;
import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.TransactionPage;
import com.expensetracker.application.port.out.TransactionRepository;
import java.util.Objects;

public final class ListTransactionsService implements ListTransactionsUseCase {

  private final TransactionRepository transactionRepository;

  public ListTransactionsService(TransactionRepository transactionRepository) {
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
  }

  @Override
  public TransactionPage execute(TransactionFilter filter) {
    return transactionRepository.findByFilter(filter);
  }
}
