package com.expensetracker.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Persiste una {@link CategoryEntity} de prueba para satisfacer las FK de
 * rules/transactions/budgets.
 */
final class CategoryFixtures {

  private CategoryFixtures() {}

  static UUID persistCategory(TestEntityManager entityManager, String name) {
    CategoryEntity category = new CategoryEntity();
    category.setId(UUID.randomUUID());
    category.setName(name);
    entityManager.persistAndFlush(category);
    return category.getId();
  }
}
