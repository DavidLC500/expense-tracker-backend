package com.expensetracker.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.CategoryAlreadyExistsException;
import com.expensetracker.domain.model.Category;
import com.expensetracker.testdata.PostgresIntegrationTest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaCategoryRepository.class, CategoryMapperImpl.class})
class JpaCategoryRepositoryTest extends PostgresIntegrationTest {

  @Autowired private JpaCategoryRepository categoryRepository;

  @Test
  void shouldSaveAndRetrieveCategoryById() {
    Category category = Category.of("Alimentación");

    categoryRepository.save(category);

    Optional<Category> found = categoryRepository.findById(category.id());

    assertThat(found).contains(category);
  }

  @Test
  void shouldReturnAllSavedCategoriesOrderedByNameOnFindAll() {
    Category leisure = Category.of("Ocio");
    Category housing = Category.of("Vivienda");
    Category food = Category.of("Alimentación");

    categoryRepository.save(leisure);
    categoryRepository.save(housing);
    categoryRepository.save(food);

    List<Category> all = categoryRepository.findAll();

    assertThat(all).containsExactly(food, leisure, housing);
  }

  @Test
  void shouldOrderAccentedAndLowercaseNamesAlphabeticallyOnFindAll() {
    Category housing = Category.of("Vivienda");
    Category payroll = Category.of("Nómina");
    Category leisure = Category.of("ocio");
    Category optics = Category.of("Óptica");

    categoryRepository.save(housing);
    categoryRepository.save(payroll);
    categoryRepository.save(leisure);
    categoryRepository.save(optics);

    assertThat(categoryRepository.findAll()).containsExactly(payroll, leisure, optics, housing);
  }

  // Simula la petición que gana la carrera al caso de uso: ha comprobado que el nombre no existía
  // y, cuando llega a guardar, otra petición ya lo ha creado. Solo la BD puede impedirlo.
  @ParameterizedTest
  @CsvSource({"Ocio, OCIO", "Alimentación, alimentacion", "'Ocio Diario', 'ocio   diario'"})
  void shouldRejectASecondCategoryWithTheSameNormalizedName(String existing, String duplicate) {
    categoryRepository.save(Category.of(existing));

    assertThatThrownBy(() -> categoryRepository.save(Category.of(duplicate)))
        .isInstanceOf(CategoryAlreadyExistsException.class);
  }

  @Test
  void shouldReturnTrueFromExistsByIdWhenCategoryIsSaved() {
    Category category = Category.of("Transporte");

    categoryRepository.save(category);

    assertThat(categoryRepository.existsById(category.id())).isTrue();
  }

  @Test
  void shouldReturnFalseFromExistsByIdWhenCategoryIsMissing() {
    assertThat(categoryRepository.existsById(UUID.randomUUID())).isFalse();
  }

  @Test
  void shouldReturnEmptyFromFindByIdWhenCategoryIsMissing() {
    Optional<Category> found = categoryRepository.findById(UUID.randomUUID());

    assertThat(found).isEmpty();
  }
}
