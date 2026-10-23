package com.expensetracker.testdata;

import com.expensetracker.application.port.out.BudgetRepository;
import com.expensetracker.domain.model.Budget;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fake en memoria de {@code BudgetRepository} para tests de casos de uso. La clave interna es el
 * par (categoryId, month): {@code save} hace upsert, igual que exige el contrato del puerto.
 */
public final class InMemoryBudgetRepository implements BudgetRepository {

  private final Map<String, Budget> budgets = new LinkedHashMap<>();

  @Override
  public Budget save(Budget budget) {
    budgets.put(key(budget.categoryId(), budget.month()), budget);
    return budget;
  }

  @Override
  public List<Budget> findByMonth(YearMonth month) {
    return budgets.values().stream().filter(budget -> budget.month().equals(month)).toList();
  }

  @Override
  public boolean delete(UUID categoryId, YearMonth month) {
    return budgets.remove(key(categoryId, month)) != null;
  }

  private static String key(UUID categoryId, YearMonth month) {
    return categoryId + "|" + month;
  }
}
