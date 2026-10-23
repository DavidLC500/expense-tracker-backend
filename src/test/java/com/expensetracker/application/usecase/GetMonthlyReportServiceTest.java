package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.MonthlyReport;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetMonthlyReportServiceTest {

  private static final YearMonth MONTH = YearMonth.of(2026, 3);

  private InMemoryTransactionRepository transactionRepository;
  private GetMonthlyReportService getMonthlyReportService;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    getMonthlyReportService = new GetMonthlyReportService(transactionRepository);
  }

  @Test
  void shouldCalculatePreviousMonthExpenseDifferenceAcrossTwoMonths() {
    UUID categoryId = UUID.randomUUID();
    transactionRepository.save(
        Transactions.expense(LocalDate.of(2026, 2, 10), "200.00", categoryId));
    transactionRepository.save(
        Transactions.expense(LocalDate.of(2026, 3, 10), "350.00", categoryId));
    transactionRepository.save(
        Transactions.income(LocalDate.of(2026, 3, 15), "1000.00", categoryId));

    MonthlyReport report = getMonthlyReportService.execute(MONTH);

    assertThat(report.month()).isEqualTo(MONTH);
    assertThat(report.totalExpense().amount()).isEqualByComparingTo("350.00");
    assertThat(report.totalIncome().amount()).isEqualByComparingTo("1000.00");
    assertThat(report.previousMonthExpenseDifference().amount()).isEqualByComparingTo("150.00");
  }
}
