package com.expensetracker.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositorio Spring Data de {@link BudgetEntity}. */
public interface BudgetJpaRepository extends JpaRepository<BudgetEntity, UUID> {

  Optional<BudgetEntity> findByCategoryIdAndMonth(UUID categoryId, LocalDate month);

  List<BudgetEntity> findByMonth(LocalDate month);

  // Un solo DELETE en la BD; el derivado de Spring Data (deleteBy...) primero carga las filas.
  @Modifying
  @Query("DELETE FROM BudgetEntity b WHERE b.categoryId = :categoryId AND b.month = :month")
  int deleteByCategoryIdAndMonth(
      @Param("categoryId") UUID categoryId, @Param("month") LocalDate month);
}
