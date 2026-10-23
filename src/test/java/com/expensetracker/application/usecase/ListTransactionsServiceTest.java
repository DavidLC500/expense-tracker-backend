package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.TransactionPage;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListTransactionsServiceTest {

  private static final UUID FOOD_CATEGORY = UUID.randomUUID();
  private static final UUID LEISURE_CATEGORY = UUID.randomUUID();

  private InMemoryTransactionRepository transactionRepository;
  private ListTransactionsService service;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    service = new ListTransactionsService(transactionRepository);
  }

  @Test
  void shouldDelegateMonthFilterToRepository() {
    Transaction march =
        transactionRepository.save(
            Transactions.expense(LocalDate.of(2026, 3, 10), "10.00", FOOD_CATEGORY));
    transactionRepository.save(
        Transactions.expense(LocalDate.of(2026, 4, 10), "10.00", FOOD_CATEGORY));

    TransactionPage page =
        service.execute(new TransactionFilter(YearMonth.of(2026, 3), null, null, 0, 20));

    assertThat(page.items()).extracting(Transaction::id).containsExactly(march.id());
    assertThat(page.totalElements()).isEqualTo(1);
  }

  @Test
  void shouldDelegateCategoryFilterToRepository() {
    Transaction foodTransaction =
        transactionRepository.save(
            Transactions.expense(LocalDate.of(2026, 3, 10), "10.00", FOOD_CATEGORY));
    transactionRepository.save(
        Transactions.expense(LocalDate.of(2026, 3, 11), "10.00", LEISURE_CATEGORY));

    TransactionPage page = service.execute(new TransactionFilter(null, FOOD_CATEGORY, null, 0, 20));

    assertThat(page.items()).extracting(Transaction::id).containsExactly(foodTransaction.id());
  }

  @Test
  void shouldDelegatePaginationToRepository() {
    for (int i = 0; i < 3; i++) {
      transactionRepository.save(
          Transactions.expense(LocalDate.of(2026, 3, 10 + i), "10.00", FOOD_CATEGORY));
    }

    TransactionPage page = service.execute(new TransactionFilter(null, null, null, 0, 2));

    assertThat(page.items()).hasSize(2);
    assertThat(page.page()).isEqualTo(0);
    assertThat(page.pageSize()).isEqualTo(2);
    assertThat(page.totalElements()).isEqualTo(3);
  }
}
