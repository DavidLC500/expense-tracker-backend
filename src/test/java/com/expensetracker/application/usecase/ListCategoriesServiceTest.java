package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Category;
import com.expensetracker.testdata.InMemoryCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListCategoriesServiceTest {

  private InMemoryCategoryRepository categoryRepository;
  private ListCategoriesService listCategoriesService;

  @BeforeEach
  void setUp() {
    categoryRepository = new InMemoryCategoryRepository();
    listCategoriesService = new ListCategoriesService(categoryRepository);
  }

  @Test
  void shouldReturnEmptyListWhenNoCategoriesExist() {
    assertThat(listCategoriesService.execute()).isEmpty();
  }

  @Test
  void shouldReturnAllPersistedCategories() {
    Category first = categoryRepository.save(Category.of("Alimentación"));
    Category second = categoryRepository.save(Category.of("Ocio"));

    assertThat(listCategoriesService.execute()).containsExactlyInAnyOrder(first, second);
  }
}
