package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.domain.model.Budget;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/** Respuesta JSON de un presupuesto, tras {@code PUT /api/budgets/{year}/{month}}. */
@Schema(description = "Presupuesto guardado.")
public record BudgetResponse(
    @Schema(description = "Id.", example = "c1d2e3f4-a5b6-4c7d-8e9f-0a1b2c3d4e5f") UUID id,
    @Schema(description = "Categoría.", example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70")
        UUID categoryId,
    @Schema(description = "Año.", example = "2026") int year,
    @Schema(description = "Mes (1-12).", example = "1") int month,
    @Schema(description = "Importe presupuestado.", example = "300.00") BigDecimal amount,
    @Schema(description = "Divisa (ISO 4217).", example = "EUR") String currency) {

  static BudgetResponse from(Budget budget) {
    return new BudgetResponse(
        budget.id(),
        budget.categoryId(),
        budget.month().getYear(),
        budget.month().getMonthValue(),
        budget.amount().amount(),
        budget.amount().currency().getCurrencyCode());
  }
}
