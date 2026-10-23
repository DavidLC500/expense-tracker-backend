package com.expensetracker.application.usecase;

import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.application.port.in.CreateRuleUseCase;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.domain.service.TextNormalizer;
import java.util.Objects;
import java.util.UUID;

public final class CreateRuleService implements CreateRuleUseCase {

  private final RuleRepository ruleRepository;
  private final CategoryRepository categoryRepository;

  public CreateRuleService(RuleRepository ruleRepository, CategoryRepository categoryRepository) {
    this.ruleRepository = Objects.requireNonNull(ruleRepository, "ruleRepository must not be null");
    this.categoryRepository =
        Objects.requireNonNull(categoryRepository, "categoryRepository must not be null");
  }

  @Override
  public Rule execute(String pattern, UUID categoryId, int priority) {
    if (!categoryRepository.existsById(categoryId)) {
      throw new CategoryNotFoundException(categoryId);
    }

    String normalizedPattern = TextNormalizer.normalize(pattern);
    Rule rule = Rule.of(normalizedPattern, categoryId, priority);
    return ruleRepository.save(rule);
  }
}
