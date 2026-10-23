package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.application.port.in.TransactionMonth;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListTransactionMonthsServiceTest {

  private static final UUID FOOD_CATEGORY = UUID.randomUUID();

  private InMemoryTransactionRepository transactionRepository;
  private ListTransactionMonthsService service;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    service = new ListTransactionMonthsService(transactionRepository);
  }

  @Test
  void shouldListMonthsWithTransactionsFromMostRecent() {
    transactionRepository.save(
        Transactions.expense(LocalDate.of(2025, 12, 31), "10.00", FOOD_CATEGORY));
    transactionRepository.save(
        Transactions.expense(LocalDate.of(2026, 2, 1), "10.00", FOOD_CATEGORY));
    transactionRepository.save(Transactions.expense(LocalDate.of(2026, 2, 28), "10.00", null));
    transactionRepository.save(
        Transactions.expense(LocalDate.of(2024, 6, 15), "10.00", FOOD_CATEGORY));

    assertThat(service.execute())
        .containsExactly(
            new TransactionMonth(YearMonth.of(2026, 2), 2),
            new TransactionMonth(YearMonth.of(2025, 12), 1),
            new TransactionMonth(YearMonth.of(2024, 6), 1));
  }

  @Test
  void shouldReturnEmptyListWhenThereAreNoTransactions() {
    assertThat(service.execute()).isEmpty();
  }
}
