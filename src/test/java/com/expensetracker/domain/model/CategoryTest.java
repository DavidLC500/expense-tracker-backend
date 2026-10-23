package com.expensetracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.domain.exception.InvalidCategoryException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CategoryTest {

  @Test
  void shouldCreateCategoryWithGivenIdAndName() {
    UUID id = UUID.randomUUID();

    Category category = new Category(id, "Alimentacion");

    assertThat(category.id()).isEqualTo(id);
    assertThat(category.name()).isEqualTo("Alimentacion");
  }

  @Test
  void shouldTrimWhitespaceFromName() {
    Category category = new Category(UUID.randomUUID(), "  Ocio  ");

    assertThat(category.name()).isEqualTo("Ocio");
  }

  @Test
  void shouldGenerateRandomIdWhenUsingOfFactory() {
    Category first = Category.of("Transporte");
    Category second = Category.of("Transporte");

    assertThat(first.id()).isNotNull().isNotEqualTo(second.id());
    assertThat(first.name()).isEqualTo("Transporte");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenIdIsNull() {
    assertThatThrownBy(() -> new Category(null, "Ocio")).isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   ", "\t", "\n"})
  void shouldThrowInvalidCategoryExceptionWhenNameIsNullOrBlank(String invalidName) {
    assertThatThrownBy(() -> new Category(UUID.randomUUID(), invalidName))
        .isInstanceOf(InvalidCategoryException.class);
  }
}
