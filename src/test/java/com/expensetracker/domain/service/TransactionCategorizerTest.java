package com.expensetracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Rule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionCategorizerTest {

  private static final UUID FOOD_CATEGORY = UUID.randomUUID();
  private static final UUID LEISURE_CATEGORY = UUID.randomUUID();
  private static final UUID SUPERMARKET_CATEGORY = UUID.randomUUID();

  @Test
  void shouldReturnEmptyWhenNoRulesMatch() {
    List<Rule> rules = List.of(Rule.of("NETFLIX", LEISURE_CATEGORY, 1));

    Optional<UUID> result = TransactionCategorizer.categorize("MERCADONA MADRID", rules);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenRuleListIsEmpty() {
    Optional<UUID> result = TransactionCategorizer.categorize("MERCADONA MADRID", List.of());

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnCategoryOfSingleMatchingRule() {
    List<Rule> rules = List.of(Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    Optional<UUID> result = TransactionCategorizer.categorize("COMPRA MERCADONA MADRID", rules);

    assertThat(result).contains(FOOD_CATEGORY);
  }

  @Test
  void shouldNormalizeConceptBeforeMatching() {
    List<Rule> rules = List.of(Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    Optional<UUID> result = TransactionCategorizer.categorize("Compra en Mércadöna, Madrid", rules);

    assertThat(result).contains(FOOD_CATEGORY);
  }

  @Test
  void shouldPreferRuleWithLowerPriorityNumberWhenPatternsOverlap() {
    // "MERCADONA" (regla de supermercado genérica) y "MERCADONA EXPRESS" (regla más
    // específica) solapan sobre el mismo concepto; gana la de priority más bajo (1),
    // independientemente de cuál sea más específica textualmente.
    List<Rule> rules =
        List.of(
            Rule.of("MERCADONA EXPRESS", SUPERMARKET_CATEGORY, 2),
            Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    Optional<UUID> result = TransactionCategorizer.categorize("MERCADONA EXPRESS MADRID", rules);

    assertThat(result).contains(FOOD_CATEGORY);
  }

  @Test
  void shouldKeepFirstRuleInListOrderWhenPrioritiesTie() {
    List<Rule> rules =
        List.of(
            Rule.of("MERCADONA", FOOD_CATEGORY, 1), Rule.of("MERCADONA", SUPERMARKET_CATEGORY, 1));

    Optional<UUID> result = TransactionCategorizer.categorize("MERCADONA MADRID", rules);

    assertThat(result).contains(FOOD_CATEGORY);
  }

  @Test
  void shouldKeepFirstRuleInListOrderWhenPrioritiesTieRegardlessOfListPosition() {
    List<Rule> rules =
        List.of(
            Rule.of("MERCADONA", SUPERMARKET_CATEGORY, 1), Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    Optional<UUID> result = TransactionCategorizer.categorize("MERCADONA MADRID", rules);

    assertThat(result).contains(SUPERMARKET_CATEGORY);
  }

  @Test
  void shouldIgnoreNonMatchingRulesEvenWithLowerPriorityNumber() {
    List<Rule> rules =
        List.of(Rule.of("NETFLIX", LEISURE_CATEGORY, 1), Rule.of("MERCADONA", FOOD_CATEGORY, 5));

    Optional<UUID> result = TransactionCategorizer.categorize("MERCADONA MADRID", rules);

    assertThat(result).contains(FOOD_CATEGORY);
  }

  @Test
  void shouldReturnEmptyWhenConceptIsNull() {
    List<Rule> rules = List.of(Rule.of("MERCADONA", FOOD_CATEGORY, 1));

    Optional<UUID> result = TransactionCategorizer.categorize(null, rules);

    assertThat(result).isEmpty();
  }
}
