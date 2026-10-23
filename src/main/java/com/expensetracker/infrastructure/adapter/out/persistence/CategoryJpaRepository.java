package com.expensetracker.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Repositorio Spring Data JPA de {@link CategoryEntity}. */
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

  /**
   * Orden alfabético sin depender de la colación de la BD: en la imagen Alpine de PostgreSQL ordena
   * byte a byte y "Nómina" quedaba detrás de "Vivienda".
   */
  @Query(value = "SELECT * FROM categories ORDER BY lower(unaccent(name)), id", nativeQuery = true)
  List<CategoryEntity> findAllOrderedByName();
}
