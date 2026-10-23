package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.domain.model.BudgetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

/** Respuesta JSON del estado de un presupuesto, de {@code GET /api/budgets/{year}/{month}}. */
@Schema(description = "Cómo va el presupuesto de una categoría en un mes.")
public record BudgetStatusResponse(
    @Schema(description = "Categoría.", example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70")
        UUID categoryId,
    @Schema(description = "Año.", example = "2026") int year,
    @Schema(description = "Mes (1-12).", example = "1") int month,
    @Schema(description = "Importe presupuestado.", example = "80.00") BigDecimal budgeted,
    @Schema(description = "Gastado en la categoría ese mes, en positivo.", example = "77.40")
        BigDecimal spent,
    @Schema(description = "Lo que queda; negativo si se ha excedido.", example = "2.60")
        BigDecimal remaining,
    @Schema(description = "Divisa (ISO 4217).", example = "EUR") String currency,
    @Schema(
            description =
                "Porcentaje usado, con 2 decimales; puede pasar de 100. Con presupuesto 0 es 0 si"
                    + " no se ha gastado nada y 100 si se ha gastado algo.",
            example = "96.75")
        BigDecimal percentageUsed,
    @Schema(description = "true si lo gastado supera lo presupuestado.", example = "false")
        boolean exceeded) {

  static BudgetStatusResponse from(BudgetStatus status) {
    return new BudgetStatusResponse(
        status.categoryId(),
        status.month().getYear(),
        status.month().getMonthValue(),
        status.budgeted().amount(),
        status.spent().amount(),
        status.remaining().amount(),
        status.budgeted().currency().getCurrencyCode(),
        status.percentageUsed(),
        status.exceeded());
  }
}
