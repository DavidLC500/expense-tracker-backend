package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.domain.model.Rule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** Adaptador de persistencia de {@link RuleRepository} sobre JPA/PostgreSQL. */
@Repository
public class JpaRuleRepository implements RuleRepository {

  private final RuleJpaRepository jpaRepository;
  private final RuleMapper mapper;

  public JpaRuleRepository(RuleJpaRepository jpaRepository, RuleMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Rule save(Rule rule) {
    RuleEntity saved = jpaRepository.save(mapper.toEntity(rule));
    return mapper.toDomain(saved);
  }

  @Override
  public List<Rule> findAll() {
    return jpaRepository.findAll(Sort.by("priority", "pattern", "id")).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaRepository.existsById(id);
  }

  // Desde Spring Data 3 deleteById ignora un id inexistente: cumple ya el contrato no-op del
  // puerto.
  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
