package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.ListCategoriesUseCase;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.domain.model.Category;
import java.util.List;
import java.util.Objects;

public final class ListCategoriesService implements ListCategoriesUseCase {

  private final CategoryRepository categoryRepository;

  public ListCategoriesService(CategoryRepository categoryRepository) {
    this.categoryRepository =
        Objects.requireNonNull(categoryRepository, "categoryRepository must not be null");
  }

  @Override
  public List<Category> execute() {
    return categoryRepository.findAll();
  }
}
