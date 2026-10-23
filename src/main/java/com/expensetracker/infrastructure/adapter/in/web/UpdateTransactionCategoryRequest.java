package com.expensetracker.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Cuerpo de la petición para corregir la categoría de un movimiento. {@code categoryId} es nullable
 * a propósito: enviarlo a {@code null} vuelve a dejar el movimiento sin categorizar.
 */
@Schema(description = "Categoría elegida a mano para un movimiento.")
public record UpdateTransactionCategoryRequest(
    @Schema(
            description = "Categoría nueva; null o ausente deja el movimiento sin categoría.",
            example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70")
        UUID categoryId) {}
