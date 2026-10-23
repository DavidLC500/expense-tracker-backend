package com.expensetracker.application.usecase;

import com.expensetracker.application.exception.RuleNotFoundException;
import com.expensetracker.application.port.in.DeleteRuleUseCase;
import com.expensetracker.application.port.out.RuleRepository;
import java.util.Objects;
import java.util.UUID;

public final class DeleteRuleService implements DeleteRuleUseCase {

  private final RuleRepository ruleRepository;

  public DeleteRuleService(RuleRepository ruleRepository) {
    this.ruleRepository = Objects.requireNonNull(ruleRepository, "ruleRepository must not be null");
  }

  @Override
  public void execute(UUID ruleId) {
    if (!ruleRepository.existsById(ruleId)) {
      throw new RuleNotFoundException(ruleId);
    }
    ruleRepository.deleteById(ruleId);
  }
}
