package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.ListTransactionMonthsUseCase;
import com.expensetracker.application.port.in.TransactionMonth;
import com.expensetracker.application.port.out.TransactionRepository;
import java.util.List;
import java.util.Objects;

public final class ListTransactionMonthsService implements ListTransactionMonthsUseCase {

  private final TransactionRepository transactionRepository;

  public ListTransactionMonthsService(TransactionRepository transactionRepository) {
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
  }

  @Override
  public List<TransactionMonth> execute() {
    return transactionRepository.countByMonth();
  }
}
