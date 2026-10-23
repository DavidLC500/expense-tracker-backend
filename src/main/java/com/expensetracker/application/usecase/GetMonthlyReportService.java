package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.GetMonthlyReportUseCase;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.MonthlyReport;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.domain.service.ReportCalculator;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public final class GetMonthlyReportService implements GetMonthlyReportUseCase {

  private final TransactionRepository transactionRepository;

  public GetMonthlyReportService(TransactionRepository transactionRepository) {
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
  }

  @Override
  public MonthlyReport execute(YearMonth month) {
    List<Transaction> transactionsThisMonth = transactionRepository.findByMonth(month);
    List<Transaction> transactionsPreviousMonth =
        transactionRepository.findByMonth(month.minusMonths(1));
    return ReportCalculator.calculate(
        month, transactionsThisMonth, transactionsPreviousMonth, Money.DEFAULT_CURRENCY);
  }
}
