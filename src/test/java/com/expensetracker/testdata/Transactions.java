package com.expensetracker.testdata;

import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

/** Builder de {@code Transaction} para tests de dominio, evita repetir los campos de relleno. */
public final class Transactions {

  private static final Currency EUR = Currency.getInstance("EUR");

  private Transactions() {}

  public static Transaction transaction(
      LocalDate date, String concept, BigDecimal amount, UUID categoryId) {
    return new Transaction(
        UUID.randomUUID(),
        date,
        concept,
        Money.of(amount, EUR),
        categoryId,
        "hash-" + UUID.randomUUID(),
        Instant.now());
  }

  public static Transaction transaction(LocalDate date, BigDecimal amount, UUID categoryId) {
    return transaction(date, "concepto de prueba", amount, categoryId);
  }

  public static Transaction transaction(LocalDate date, String amount, UUID categoryId) {
    return transaction(date, new BigDecimal(amount), categoryId);
  }

  public static Transaction expense(LocalDate date, String absoluteAmount, UUID categoryId) {
    return transaction(date, new BigDecimal(absoluteAmount).negate(), categoryId);
  }

  public static Transaction expense(
      LocalDate date, String concept, String absoluteAmount, UUID categoryId) {
    return transaction(date, concept, new BigDecimal(absoluteAmount).negate(), categoryId);
  }

  public static Transaction income(LocalDate date, String amount, UUID categoryId) {
    return transaction(date, new BigDecimal(amount), categoryId);
  }
}
