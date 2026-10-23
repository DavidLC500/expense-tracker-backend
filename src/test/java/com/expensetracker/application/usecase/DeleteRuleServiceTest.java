package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.RuleNotFoundException;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.testdata.InMemoryRuleRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteRuleServiceTest {

  private InMemoryRuleRepository ruleRepository;
  private DeleteRuleService deleteRuleService;

  @BeforeEach
  void setUp() {
    ruleRepository = new InMemoryRuleRepository();
    deleteRuleService = new DeleteRuleService(ruleRepository);
  }

  @Test
  void shouldDeleteExistingRule() {
    Rule rule = ruleRepository.save(Rule.of("MERCADONA", UUID.randomUUID(), 1));

    deleteRuleService.execute(rule.id());

    assertThat(ruleRepository.findAll()).isEmpty();
  }

  @Test
  void shouldThrowRuleNotFoundExceptionWhenRuleDoesNotExist() {
    UUID unknownRuleId = UUID.randomUUID();

    assertThatThrownBy(() -> deleteRuleService.execute(unknownRuleId))
        .isInstanceOf(RuleNotFoundException.class);
  }

  @Test
  void shouldNotAffectOtherRulesWhenDeletingOne() {
    Rule toKeep = ruleRepository.save(Rule.of("MERCADONA", UUID.randomUUID(), 1));
    Rule toDelete = ruleRepository.save(Rule.of("NETFLIX", UUID.randomUUID(), 2));

    deleteRuleService.execute(toDelete.id());

    assertThat(ruleRepository.findAll()).containsExactly(toKeep);
  }
}
