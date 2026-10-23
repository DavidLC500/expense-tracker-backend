package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.domain.model.Rule;
import org.mapstruct.Mapper;

/** Mapeo entre {@link Rule} (dominio) y {@link RuleEntity} (JPA). */
@Mapper(componentModel = "spring")
public interface RuleMapper {

  Rule toDomain(RuleEntity entity);

  RuleEntity toEntity(Rule rule);
}
