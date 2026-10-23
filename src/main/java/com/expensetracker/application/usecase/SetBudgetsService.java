package com.expensetracker.application.usecase;

import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.application.port.in.BudgetAmount;
import com.expensetracker.application.port.in.SetBudgetsUseCase;
import com.expensetracker.application.port.out.BudgetRepository;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.domain.model.Budget;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

public final class SetBudgetsService implements SetBudgetsUseCase {

  private final BudgetRepository budgetRepository;
  private final CategoryRepository categoryRepository;

  public SetBudgetsService(
      BudgetRepository budgetRepository, CategoryRepository categoryRepository) {
    this.budgetRepository =
        Objects.requireNonNull(budgetRepository, "budgetRepository must not be null");
    this.categoryRepository =
        Objects.requireNonNull(categoryRepository, "categoryRepository must not be null");
  }

  // Primero se validan todas las categorías: con una que no existe no se guarda ninguna, en vez de
  // dejar guardadas las anteriores y responder 404.
  @Override
  public List<Budget> execute(YearMonth month, List<BudgetAmount> budgetAmounts) {
    for (BudgetAmount budgetAmount : budgetAmounts) {
      if (!categoryRepository.existsById(budgetAmount.categoryId())) {
        throw new CategoryNotFoundException(budgetAmount.categoryId());
      }
    }
    return budgetAmounts.stream()
        .map(
            budgetAmount ->
                budgetRepository.save(
                    Budget.of(budgetAmount.categoryId(), month, budgetAmount.amount())))
        .toList();
  }
}
