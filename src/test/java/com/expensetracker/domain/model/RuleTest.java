package com.expensetracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.domain.exception.InvalidRuleException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RuleTest {

  @Test
  void shouldCreateRuleWithGivenFields() {
    UUID id = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    Rule rule = new Rule(id, "MERCADONA", categoryId, 1);

    assertThat(rule.id()).isEqualTo(id);
    assertThat(rule.pattern()).isEqualTo("MERCADONA");
    assertThat(rule.categoryId()).isEqualTo(categoryId);
    assertThat(rule.priority()).isEqualTo(1);
  }

  @Test
  void shouldGenerateRandomIdWhenUsingOfFactory() {
    UUID categoryId = UUID.randomUUID();

    Rule rule = Rule.of("NETFLIX", categoryId, 2);

    assertThat(rule.id()).isNotNull();
    assertThat(rule.pattern()).isEqualTo("NETFLIX");
    assertThat(rule.categoryId()).isEqualTo(categoryId);
    assertThat(rule.priority()).isEqualTo(2);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenIdIsNull() {
    assertThatThrownBy(() -> new Rule(null, "MERCADONA", UUID.randomUUID(), 1))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenCategoryIdIsNull() {
    assertThatThrownBy(() -> new Rule(UUID.randomUUID(), "MERCADONA", null, 1))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   ", "\t", "\n"})
  void shouldThrowInvalidRuleExceptionWhenPatternIsNullOrBlank(String invalidPattern) {
    assertThatThrownBy(() -> new Rule(UUID.randomUUID(), invalidPattern, UUID.randomUUID(), 1))
        .isInstanceOf(InvalidRuleException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
  void shouldAcceptAnyPriorityIncludingNegativeAndExtremeValues(int priority) {
    Rule rule = new Rule(UUID.randomUUID(), "MERCADONA", UUID.randomUUID(), priority);

    assertThat(rule.priority()).isEqualTo(priority);
  }
}
