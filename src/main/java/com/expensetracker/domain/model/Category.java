package com.expensetracker.domain.model;

import com.expensetracker.domain.exception.InvalidCategoryException;
import java.util.Objects;
import java.util.UUID;

/**
 * Categoría de gasto o ingreso (p. ej. "Alimentación", "Ocio").
 *
 * <p>El nombre se normaliza con {@code trim()} en el constructor: dos categorías que difieran solo
 * en espacios al principio o al final deben tratarse como la misma entrada de dominio.
 */
public record Category(UUID id, String name) {

  public Category {
    Objects.requireNonNull(id, "id must not be null");
    if (name == null || name.isBlank()) {
      throw new InvalidCategoryException("Category name must not be null or blank");
    }
    name = name.trim();
  }

  public static Category of(String name) {
    return new Category(UUID.randomUUID(), name);
  }
}
