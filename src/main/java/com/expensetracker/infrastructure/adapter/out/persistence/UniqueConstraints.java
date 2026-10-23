package com.expensetracker.infrastructure.adapter.out.persistence;

import org.springframework.dao.DataIntegrityViolationException;

/** Distingue qué restricción única de la BD ha provocado una violación de integridad. */
final class UniqueConstraints {

  static final String BUDGET_CATEGORY_MONTH = "uq_budgets_category_month";
  static final String CATEGORY_NORMALIZED_NAME = "uq_categories_normalized_name";

  private UniqueConstraints() {}

  // PostgreSQL incluye el nombre de la restricción en el mensaje de la excepción original.
  static boolean isViolationOf(DataIntegrityViolationException exception, String constraint) {
    return String.valueOf(exception.getMostSpecificCause().getMessage()).contains(constraint);
  }
}
