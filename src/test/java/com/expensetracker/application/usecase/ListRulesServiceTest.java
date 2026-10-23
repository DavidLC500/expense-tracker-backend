package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Rule;
import com.expensetracker.testdata.InMemoryRuleRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListRulesServiceTest {

  private InMemoryRuleRepository ruleRepository;
  private ListRulesService listRulesService;

  @BeforeEach
  void setUp() {
    ruleRepository = new InMemoryRuleRepository();
    listRulesService = new ListRulesService(ruleRepository);
  }

  @Test
  void shouldReturnEmptyListWhenNoRulesExist() {
    assertThat(listRulesService.execute()).isEmpty();
  }

  @Test
  void shouldReturnAllPersistedRules() {
    Rule first = ruleRepository.save(Rule.of("MERCADONA", UUID.randomUUID(), 1));
    Rule second = ruleRepository.save(Rule.of("NETFLIX", UUID.randomUUID(), 2));

    assertThat(listRulesService.execute()).containsExactlyInAnyOrder(first, second);
  }
}
