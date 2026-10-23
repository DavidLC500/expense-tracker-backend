package com.expensetracker.domain.model;

import com.expensetracker.domain.exception.InvalidRuleException;
import java.util.Objects;
import java.util.UUID;

/**
 * Regla de categorización automática: si el concepto de una transacción contiene {@code pattern},
 * se le asigna la categoría {@code categoryId}.
 *
 * <p>Convención de prioridad: a menor valor de {@code priority}, mayor prioridad (1 gana sobre 2).
 * {@code pattern} se asume ya normalizado (sin tildes, en mayúsculas); esta clase no normaliza
 * nada, esa responsabilidad es de quien construye la regla.
 */
public record Rule(UUID id, String pattern, UUID categoryId, int priority) {

  public Rule {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(categoryId, "categoryId must not be null");
    if (pattern == null || pattern.isBlank()) {
      throw new InvalidRuleException("Rule pattern must not be null or blank");
    }
  }

  public static Rule of(String pattern, UUID categoryId, int priority) {
    return new Rule(UUID.randomUUID(), pattern, categoryId, priority);
  }
}
