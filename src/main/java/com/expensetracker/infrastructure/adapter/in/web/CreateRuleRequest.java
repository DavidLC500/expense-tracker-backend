package com.expensetracker.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Cuerpo de la petición para crear una regla. {@code pattern} coincide con VARCHAR(255) de la BD.
 * {@code priority} es {@code Integer} (no {@code int}) para que {@code @NotNull} obligue al cliente
 * a enviarlo explícitamente: con un primitivo, omitir el campo se convertiría en silenciosamente
 * "máxima prioridad" (0).
 */
@Schema(description = "Regla nueva.")
public record CreateRuleRequest(
    @Schema(
            description =
                "Texto que debe contener el concepto; no distingue mayúsculas ni tildes y se"
                    + " guarda normalizado.",
            example = "mercadona")
        @NotBlank
        @Size(max = 255)
        String pattern,
    @Schema(
            description = "Categoría que asigna la regla.",
            example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70")
        @NotNull
        UUID categoryId,
    @Schema(
            description =
                "Si varias reglas coinciden, gana la de número más bajo. Admite negativos.",
            example = "10")
        @NotNull
        Integer priority) {}
