package com.expensetracker.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Cuerpo de la petición de {@code PUT /api/budgets/{year}/{month}}. */
@Schema(description = "Presupuestos a guardar para el mes.")
public record SetBudgetsRequest(
    @Schema(description = "Al menos uno; las categorías que no se envían no cambian.")
        @NotEmpty
        @Valid
        List<BudgetAmountRequest> budgets) {

  public SetBudgetsRequest {
    budgets = budgets == null ? List.of() : List.copyOf(budgets);
  }
}
