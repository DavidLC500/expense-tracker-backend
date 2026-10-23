package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.domain.model.Category;
import org.mapstruct.Mapper;

/** Mapeo entre {@link Category} (dominio) y {@link CategoryEntity} (JPA). */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

  Category toDomain(CategoryEntity entity);

  CategoryEntity toEntity(Category category);
}
