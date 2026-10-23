package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.BudgetAmount;
import com.expensetracker.domain.model.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Importe de presupuesto para una categoría dentro de {@link SetBudgetsRequest}. {@code @Digits}
 * coincide con budgets.amount NUMERIC(14,2) de la BD.
 */
@Schema(description = "Presupuesto de una categoría.")
public record BudgetAmountRequest(
    @Schema(description = "Categoría.", example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70") @NotNull
        UUID categoryId,
    @Schema(
            description = "Importe máximo de gasto del mes, en euros (0 o más, 2 decimales).",
            example = "300.00")
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        @Digits(integer = 12, fraction = 2)
        BigDecimal amount) {

  BudgetAmount toBudgetAmount() {
    return new BudgetAmount(categoryId, Money.of(amount, Money.DEFAULT_CURRENCY));
  }
}
