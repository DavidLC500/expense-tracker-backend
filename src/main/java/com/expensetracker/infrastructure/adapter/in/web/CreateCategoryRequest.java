package com.expensetracker.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición para crear una categoría. {@code name} coincide con VARCHAR(255) de la BD.
 */
@Schema(description = "Categoría nueva.")
public record CreateCategoryRequest(
    @Schema(
            description = "Nombre; único sin distinguir mayúsculas, tildes ni espacios de más.",
            example = "Alimentación")
        @NotBlank
        @Size(max = 255)
        String name) {}
