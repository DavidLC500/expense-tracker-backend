package com.expensetracker.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data JPA de {@link RuleEntity}. */
public interface RuleJpaRepository extends JpaRepository<RuleEntity, UUID> {}
