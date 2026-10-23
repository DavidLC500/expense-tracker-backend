package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.CreateCategoryUseCase;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.domain.model.Category;
import java.util.Objects;

public final class CreateCategoryService implements CreateCategoryUseCase {

  private final CategoryRepository categoryRepository;

  public CreateCategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository =
        Objects.requireNonNull(categoryRepository, "categoryRepository must not be null");
  }

  // Los duplicados los rechaza el repositorio (CategoryAlreadyExistsException): comprobarlo aquí
  // antes de guardar no protege de dos peticiones simultáneas, la base de datos sí.
  @Override
  public Category execute(String name) {
    return categoryRepository.save(Category.of(name));
  }
}
