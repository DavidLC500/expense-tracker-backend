package com.expensetracker.application.usecase;

import com.expensetracker.application.exception.BudgetNotFoundException;
import com.expensetracker.application.port.in.DeleteBudgetUseCase;
import com.expensetracker.application.port.out.BudgetRepository;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

public final class DeleteBudgetService implements DeleteBudgetUseCase {

  private final BudgetRepository budgetRepository;

  public DeleteBudgetService(BudgetRepository budgetRepository) {
    this.budgetRepository =
        Objects.requireNonNull(budgetRepository, "budgetRepository must not be null");
  }

  // Sin comprobar antes si existe: el borrado ya dice si había fila, y así dos peticiones a la vez
  // no pueden ver las dos "existe" (una borra y la otra recibe el 404).
  @Override
  public void execute(YearMonth month, UUID categoryId) {
    if (!budgetRepository.delete(categoryId, month)) {
      throw new BudgetNotFoundException(categoryId, month);
    }
  }
}
