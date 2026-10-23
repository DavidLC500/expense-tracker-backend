package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.BudgetStatus;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.Money;
import com.expensetracker.testdata.InMemoryBudgetRepository;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetBudgetStatusServiceTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final YearMonth MONTH = YearMonth.of(2026, 3);

  private InMemoryBudgetRepository budgetRepository;
  private InMemoryTransactionRepository transactionRepository;
  private GetBudgetStatusService getBudgetStatusService;

  @BeforeEach
  void setUp() {
    budgetRepository = new InMemoryBudgetRepository();
    transactionRepository = new InMemoryTransactionRepository();
    getBudgetStatusService = new GetBudgetStatusService(budgetRepository, transactionRepository);
  }

  @Test
  void shouldReturnEmptyListWhenNoBudgetsForMonth() {
    assertThat(getBudgetStatusService.execute(MONTH)).isEmpty();
  }

  @Test
  void shouldMarkBudgetAsExceededWhenRealSpendingSurpassesIt() {
    Category food = Category.of("Alimentación");
    budgetRepository.save(Budget.of(food.id(), MONTH, Money.of(new BigDecimal("100.00"), EUR)));
    transactionRepository.save(Transactions.expense(LocalDate.of(2026, 3, 5), "60.00", food.id()));
    transactionRepository.save(Transactions.expense(LocalDate.of(2026, 3, 10), "70.00", food.id()));

    List<BudgetStatus> result = getBudgetStatusService.execute(MONTH);

    assertThat(result).hasSize(1);
    BudgetStatus status = result.get(0);
    assertThat(status.categoryId()).isEqualTo(food.id());
    assertThat(status.spent().amount()).isEqualByComparingTo("130.00");
    assertThat(status.remaining().amount()).isEqualByComparingTo("-30.00");
    assertThat(status.exceeded()).isTrue();
  }
}
