package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.BudgetNotFoundException;
import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Money;
import com.expensetracker.testdata.InMemoryBudgetRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteBudgetServiceTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final YearMonth MARCH = YearMonth.of(2026, 3);
  private static final YearMonth APRIL = YearMonth.of(2026, 4);

  private InMemoryBudgetRepository budgetRepository;
  private DeleteBudgetService service;

  @BeforeEach
  void setUp() {
    budgetRepository = new InMemoryBudgetRepository();
    service = new DeleteBudgetService(budgetRepository);
  }

  private Budget saveBudget(UUID categoryId, YearMonth month) {
    return budgetRepository.save(
        Budget.of(categoryId, month, Money.of(new BigDecimal("100.00"), EUR)));
  }

  @Test
  void shouldDeleteOnlyTheBudgetOfThatCategoryAndMonth() {
    UUID food = UUID.randomUUID();
    UUID leisure = UUID.randomUUID();
    saveBudget(food, MARCH);
    Budget leisureInMarch = saveBudget(leisure, MARCH);
    Budget foodInApril = saveBudget(food, APRIL);

    service.execute(MARCH, food);

    assertThat(budgetRepository.findByMonth(MARCH)).containsExactly(leisureInMarch);
    assertThat(budgetRepository.findByMonth(APRIL)).containsExactly(foodInApril);
  }

  @Test
  void shouldThrowBudgetNotFoundExceptionWhenThereIsNoBudget() {
    UUID food = UUID.randomUUID();
    saveBudget(food, APRIL);

    assertThatThrownBy(() -> service.execute(MARCH, food))
        .isInstanceOf(BudgetNotFoundException.class)
        .hasMessageContaining(food.toString())
        .hasMessageContaining("2026-03");
  }

  @Test
  void shouldThrowBudgetNotFoundExceptionWhenDeletingTwice() {
    UUID food = UUID.randomUUID();
    saveBudget(food, MARCH);
    service.execute(MARCH, food);

    assertThatThrownBy(() -> service.execute(MARCH, food))
        .isInstanceOf(BudgetNotFoundException.class);
  }
}
