package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.ListRulesUseCase;
import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.domain.model.Rule;
import java.util.List;
import java.util.Objects;

public final class ListRulesService implements ListRulesUseCase {

  private final RuleRepository ruleRepository;

  public ListRulesService(RuleRepository ruleRepository) {
    this.ruleRepository = Objects.requireNonNull(ruleRepository, "ruleRepository must not be null");
  }

  @Override
  public List<Rule> execute() {
    return ruleRepository.findAll();
  }
}
