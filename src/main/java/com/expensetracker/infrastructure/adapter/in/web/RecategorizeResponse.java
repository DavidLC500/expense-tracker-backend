package com.expensetracker.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

/** Respuesta de {@code POST /api/transactions/recategorize}. */
@Schema(description = "Resultado de reaplicar las reglas.")
public record RecategorizeResponse(
    @Schema(description = "Movimientos que han cambiado de categoría.", example = "12")
        int changed) {}
