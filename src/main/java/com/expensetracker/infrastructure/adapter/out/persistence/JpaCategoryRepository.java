package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.application.exception.CategoryAlreadyExistsException;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida que implementa {@link CategoryRepository} contra PostgreSQL vía JPA. La
 * unicidad del nombre la garantiza el índice {@code uq_categories_normalized_name}
 * (V7__unique_category_normalized_name.sql), también ante peticiones simultáneas.
 */
@Repository
public class JpaCategoryRepository implements CategoryRepository {

  private final CategoryJpaRepository jpaRepository;
  private final CategoryMapper mapper;

  public JpaCategoryRepository(CategoryJpaRepository jpaRepository, CategoryMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Category save(Category category) {
    try {
      // saveAndFlush: el INSERT se ejecuta ya, dentro de este try, y no al confirmar después.
      return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(category)));
    } catch (DataIntegrityViolationException e) {
      if (!UniqueConstraints.isViolationOf(e, UniqueConstraints.CATEGORY_NORMALIZED_NAME)) {
        throw e;
      }
      throw new CategoryAlreadyExistsException("Category already exists: " + category.name());
    }
  }

  @Override
  public Optional<Category> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Category> findAll() {
    return jpaRepository.findAllOrderedByName().stream().map(mapper::toDomain).toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaRepository.existsById(id);
  }
}
