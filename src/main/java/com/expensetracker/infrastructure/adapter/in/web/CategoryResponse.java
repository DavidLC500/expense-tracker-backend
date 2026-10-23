package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.domain.model.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Respuesta JSON de una categoría. */
@Schema(description = "Categoría.")
public record CategoryResponse(
    @Schema(description = "Id.", example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70") UUID id,
    @Schema(description = "Nombre tal como se creó.", example = "Alimentación") String name) {

  static CategoryResponse from(Category category) {
    return new CategoryResponse(category.id(), category.name());
  }
}
