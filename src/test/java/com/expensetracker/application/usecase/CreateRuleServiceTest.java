package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.testdata.InMemoryCategoryRepository;
import com.expensetracker.testdata.InMemoryRuleRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateRuleServiceTest {

  private InMemoryRuleRepository ruleRepository;
  private InMemoryCategoryRepository categoryRepository;
  private CreateRuleService createRuleService;

  @BeforeEach
  void setUp() {
    ruleRepository = new InMemoryRuleRepository();
    categoryRepository = new InMemoryCategoryRepository();
    createRuleService = new CreateRuleService(ruleRepository, categoryRepository);
  }

  @Test
  void shouldCreateAndPersistRuleWhenCategoryExists() {
    Category category = categoryRepository.save(Category.of("Alimentación"));

    Rule rule = createRuleService.execute("Mercadona", category.id(), 1);

    assertThat(rule.id()).isNotNull();
    assertThat(rule.pattern()).isEqualTo("MERCADONA");
    assertThat(rule.categoryId()).isEqualTo(category.id());
    assertThat(rule.priority()).isEqualTo(1);
    assertThat(ruleRepository.findAll()).containsExactly(rule);
  }

  @Test
  void shouldNormalizePatternBeforePersistingIt() {
    Category category = categoryRepository.save(Category.of("Ocio"));

    Rule rule = createRuleService.execute("  nëtflíx  ", category.id(), 2);

    assertThat(rule.pattern()).isEqualTo("NETFLIX");
  }

  @Test
  void shouldThrowCategoryNotFoundExceptionWhenCategoryDoesNotExist() {
    UUID unknownCategoryId = UUID.randomUUID();

    assertThatThrownBy(() -> createRuleService.execute("Mercadona", unknownCategoryId, 1))
        .isInstanceOf(CategoryNotFoundException.class);
    assertThat(ruleRepository.findAll()).isEmpty();
  }
}
