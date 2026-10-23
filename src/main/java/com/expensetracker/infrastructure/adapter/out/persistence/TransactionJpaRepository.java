package com.expensetracker.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Repositorio Spring Data JPA de {@link TransactionEntity}. */
public interface TransactionJpaRepository
    extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

  List<TransactionEntity> findByDateBetween(LocalDate start, LocalDate end);
}
