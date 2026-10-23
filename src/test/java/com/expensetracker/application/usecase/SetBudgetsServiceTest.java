package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.application.port.in.BudgetAmount;
import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.Money;
import com.expensetracker.testdata.InMemoryBudgetRepository;
import com.expensetracker.testdata.InMemoryCategoryRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SetBudgetsServiceTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final YearMonth MONTH = YearMonth.of(2026, 3);

  private InMemoryBudgetRepository budgetRepository;
  private InMemoryCategoryRepository categoryRepository;
  private SetBudgetsService setBudgetsService;

  @BeforeEach
  void setUp() {
    budgetRepository = new InMemoryBudgetRepository();
    categoryRepository = new InMemoryCategoryRepository();
    setBudgetsService = new SetBudgetsService(budgetRepository, categoryRepository);
  }

  @Test
  void shouldSaveNewBudgetsForExistingCategories() {
    Category food = categoryRepository.save(Category.of("Alimentación"));
    Category leisure = categoryRepository.save(Category.of("Ocio"));
    List<BudgetAmount> budgetAmounts =
        List.of(
            new BudgetAmount(food.id(), Money.of(new BigDecimal("300.00"), EUR)),
            new BudgetAmount(leisure.id(), Money.of(new BigDecimal("100.00"), EUR)));

    List<Budget> result = setBudgetsService.execute(MONTH, budgetAmounts);

    assertThat(result).hasSize(2);
    assertThat(budgetRepository.findByMonth(MONTH))
        .extracting(Budget::categoryId)
        .containsExactlyInAnyOrder(food.id(), leisure.id());
  }

  @Test
  void shouldOverwriteExistingBudgetForSameCategoryAndMonth() {
    Category food = categoryRepository.save(Category.of("Alimentación"));
    setBudgetsService.execute(
        MONTH, List.of(new BudgetAmount(food.id(), Money.of(new BigDecimal("300.00"), EUR))));

    setBudgetsService.execute(
        MONTH, List.of(new BudgetAmount(food.id(), Money.of(new BigDecimal("450.00"), EUR))));

    List<Budget> budgetsForMonth = budgetRepository.findByMonth(MONTH);
    assertThat(budgetsForMonth).hasSize(1);
    assertThat(budgetsForMonth.get(0).amount().amount()).isEqualByComparingTo("450.00");
  }

  @Test
  void shouldThrowCategoryNotFoundExceptionWhenCategoryDoesNotExist() {
    UUID unknownCategoryId = UUID.randomUUID();
    List<BudgetAmount> budgetAmounts =
        List.of(new BudgetAmount(unknownCategoryId, Money.of(new BigDecimal("100.00"), EUR)));

    assertThatThrownBy(() -> setBudgetsService.execute(MONTH, budgetAmounts))
        .isInstanceOf(CategoryNotFoundException.class);
    assertThat(budgetRepository.findByMonth(MONTH)).isEmpty();
  }

  @Test
  void shouldSaveNothingWhenAnyCategoryDoesNotExistEvenIfOthersComeFirst() {
    Category food = categoryRepository.save(Category.of("Alimentación"));
    Category leisure = categoryRepository.save(Category.of("Ocio"));
    setBudgetsService.execute(
        MONTH, List.of(new BudgetAmount(leisure.id(), Money.of(new BigDecimal("50.00"), EUR))));
    List<BudgetAmount> budgetAmounts =
        List.of(
            new BudgetAmount(food.id(), Money.of(new BigDecimal("300.00"), EUR)),
            new BudgetAmount(leisure.id(), Money.of(new BigDecimal("80.00"), EUR)),
            new BudgetAmount(UUID.randomUUID(), Money.of(new BigDecimal("100.00"), EUR)));

    assertThatThrownBy(() -> setBudgetsService.execute(MONTH, budgetAmounts))
        .isInstanceOf(CategoryNotFoundException.class);

    assertThat(budgetRepository.findByMonth(MONTH))
        .singleElement()
        .satisfies(
            budget -> {
              assertThat(budget.categoryId()).isEqualTo(leisure.id());
              assertThat(budget.amount().amount()).isEqualByComparingTo("50.00");
            });
  }
}
