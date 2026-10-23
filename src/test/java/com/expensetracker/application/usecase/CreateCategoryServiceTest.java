package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.CategoryAlreadyExistsException;
import com.expensetracker.domain.exception.InvalidCategoryException;
import com.expensetracker.domain.model.Category;
import com.expensetracker.testdata.InMemoryCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CreateCategoryServiceTest {

  private InMemoryCategoryRepository categoryRepository;
  private CreateCategoryService createCategoryService;

  @BeforeEach
  void setUp() {
    categoryRepository = new InMemoryCategoryRepository();
    createCategoryService = new CreateCategoryService(categoryRepository);
  }

  @Test
  void shouldCreateAndPersistCategory() {
    Category category = createCategoryService.execute("Alimentación");

    assertThat(category.id()).isNotNull();
    assertThat(category.name()).isEqualTo("Alimentación");
    assertThat(categoryRepository.findById(category.id())).contains(category);
  }

  @ParameterizedTest
  @ValueSource(strings = {"Alimentación", "alimentacion", "  ALIMENTACIÓN  "})
  void shouldRejectCategoryWhoseNameAlreadyExistsIgnoringCaseAndAccents(String duplicateName) {
    createCategoryService.execute("Alimentación");

    assertThatThrownBy(() -> createCategoryService.execute(duplicateName))
        .isInstanceOf(CategoryAlreadyExistsException.class);
    assertThat(categoryRepository.findAll()).hasSize(1);
  }

  @Test
  void shouldThrowInvalidCategoryExceptionWhenNameIsBlank() {
    assertThatThrownBy(() -> createCategoryService.execute("   "))
        .isInstanceOf(InvalidCategoryException.class);
  }
}
