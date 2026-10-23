package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.GetBudgetStatusUseCase;
import com.expensetracker.application.port.out.BudgetRepository;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.BudgetStatus;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.domain.service.BudgetCalculator;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public final class GetBudgetStatusService implements GetBudgetStatusUseCase {

  private final BudgetRepository budgetRepository;
  private final TransactionRepository transactionRepository;

  public GetBudgetStatusService(
      BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
    this.budgetRepository =
        Objects.requireNonNull(budgetRepository, "budgetRepository must not be null");
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
  }

  @Override
  public List<BudgetStatus> execute(YearMonth month) {
    List<Budget> budgets = budgetRepository.findByMonth(month);
    List<Transaction> transactions = transactionRepository.findByMonth(month);
    return BudgetCalculator.calculate(month, budgets, transactions);
  }
}
