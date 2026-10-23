package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.domain.model.Rule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Respuesta JSON de una regla de categorización. */
@Schema(description = "Regla de categorización.")
public record RuleResponse(
    @Schema(description = "Id.", example = "9c4e2a71-3b5d-4f86-8e1a-7d2b6c0f9e34") UUID id,
    @Schema(description = "Patrón normalizado (sin tildes, en mayúsculas).", example = "MERCADONA")
        String pattern,
    @Schema(description = "Categoría que asigna.", example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70")
        UUID categoryId,
    @Schema(description = "Prioridad; gana la de número más bajo.", example = "10") int priority) {

  static RuleResponse from(Rule rule) {
    return new RuleResponse(rule.id(), rule.pattern(), rule.categoryId(), rule.priority());
  }
}
