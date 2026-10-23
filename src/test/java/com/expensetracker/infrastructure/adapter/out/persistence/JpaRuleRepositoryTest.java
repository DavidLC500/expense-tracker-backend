package com.expensetracker.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Rule;
import com.expensetracker.testdata.PostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaRuleRepository.class, RuleMapperImpl.class})
class JpaRuleRepositoryTest extends PostgresIntegrationTest {

  @Autowired private JpaRuleRepository ruleRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  void shouldSaveAndRetrieveRuleViaFindAll() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    Rule rule = Rule.of("MERCADONA", categoryId, 1);

    ruleRepository.save(rule);

    assertThat(ruleRepository.findAll()).containsExactly(rule);
  }

  @Test
  void shouldReturnRulesOrderedByPriorityThenPattern() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Ocio");
    Rule lowPriority = ruleRepository.save(Rule.of("NETFLIX", categoryId, 20));
    Rule tieB = ruleRepository.save(Rule.of("SPOTIFY", categoryId, 10));
    Rule tieA = ruleRepository.save(Rule.of("HBO", categoryId, 10));

    assertThat(ruleRepository.findAll()).containsExactly(tieA, tieB, lowPriority);
  }

  @Test
  void shouldDeleteByIdRemoveExistingRule() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Ocio");
    Rule rule = Rule.of("NETFLIX", categoryId, 1);
    ruleRepository.save(rule);

    ruleRepository.deleteById(rule.id());

    assertThat(ruleRepository.findAll()).isEmpty();
  }

  @Test
  void shouldTellWhetherARuleExists() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Ocio");
    Rule rule = ruleRepository.save(Rule.of("NETFLIX", categoryId, 1));

    assertThat(ruleRepository.existsById(rule.id())).isTrue();
    assertThat(ruleRepository.existsById(UUID.randomUUID())).isFalse();
  }

  @Test
  void shouldNotThrowWhenDeletingByIdOfNonExistentRule() {
    assertThat(ruleRepository.findAll()).isEmpty();

    ruleRepository.deleteById(UUID.randomUUID());
  }
}
