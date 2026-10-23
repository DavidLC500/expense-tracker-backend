package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.domain.model.CategoryExpense;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gasto total de una categoría dentro del informe mensual. {@code categoryId} nulo = sin categoría.
 */
@Schema(description = "Gasto de una categoría en el mes.")
public record CategoryExpenseResponse(
    @Schema(
            description = "Categoría; null agrupa los movimientos sin categoría.",
            example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70")
        UUID categoryId,
    @Schema(description = "Total gastado, en positivo.", example = "77.40") BigDecimal total) {

  static CategoryExpenseResponse from(CategoryExpense expense) {
    return new CategoryExpenseResponse(expense.categoryId(), expense.total().amount());
  }
}
