package com.expensetracker.domain.model;

import com.expensetracker.domain.exception.InvalidTransactionException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Movimiento bancario. {@code categoryId} es el único campo que puede ser {@code null}: representa
 * "Sin categoría" (todavía no categorizado o sin regla que le aplique).
 *
 * <p>{@code manuallyCategorized} indica que el usuario fijó la categoría a mano: reaplicar las
 * reglas no la cambia.
 */
public record Transaction(
    UUID id,
    LocalDate date,
    String concept,
    Money amount,
    UUID categoryId,
    String importHash,
    Instant createdAt,
    boolean manuallyCategorized) {

  public Transaction {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(date, "date must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(importHash, "importHash must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    if (concept == null || concept.isBlank()) {
      throw new InvalidTransactionException("concept must not be null or blank");
    }
  }

  /** Movimiento categorizado por reglas, aún sin corrección manual. */
  public Transaction(
      UUID id,
      LocalDate date,
      String concept,
      Money amount,
      UUID categoryId,
      String importHash,
      Instant createdAt) {
    this(id, date, concept, amount, categoryId, importHash, createdAt, false);
  }

  /**
   * Categoría decidida por las reglas ({@code null} si ninguna coincide): deja de ser una
   * corrección manual, así que reaplicar las reglas puede volver a cambiarla.
   */
  public Transaction withRuleCategory(UUID newCategoryId) {
    return new Transaction(id, date, concept, amount, newCategoryId, importHash, createdAt, false);
  }

  /**
   * Categoría elegida por el usuario ({@code null} = "Sin categoría"); las reglas ya no la tocan.
   */
  public Transaction withManualCategory(UUID newCategoryId) {
    return new Transaction(id, date, concept, amount, newCategoryId, importHash, createdAt, true);
  }

  public boolean isUncategorized() {
    return categoryId == null;
  }
}
