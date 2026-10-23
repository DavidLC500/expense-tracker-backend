package com.expensetracker.domain.model;

import com.expensetracker.domain.exception.InvalidBudgetException;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

/** Presupuesto mensual asignado a una categoría. */
public record Budget(UUID id, UUID categoryId, YearMonth month, Money amount) {

  public Budget {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(categoryId, "categoryId must not be null");
    Objects.requireNonNull(month, "month must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    if (amount.isNegative()) {
      throw new InvalidBudgetException("Budget amount must not be negative: " + amount);
    }
  }

  public static Budget of(UUID categoryId, YearMonth month, Money amount) {
    return new Budget(UUID.randomUUID(), categoryId, month, amount);
  }
}
