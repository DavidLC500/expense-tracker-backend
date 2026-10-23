package com.expensetracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.domain.exception.InvalidBudgetException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BudgetTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final YearMonth MONTH = YearMonth.of(2026, 10);

  @Test
  void shouldCreateBudgetWithGivenFields() {
    UUID id = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    Money amount = Money.of(new BigDecimal("300.00"), EUR);

    Budget budget = new Budget(id, categoryId, MONTH, amount);

    assertThat(budget.id()).isEqualTo(id);
    assertThat(budget.categoryId()).isEqualTo(categoryId);
    assertThat(budget.month()).isEqualTo(MONTH);
    assertThat(budget.amount()).isEqualTo(amount);
  }

  @Test
  void shouldGenerateRandomIdWhenUsingOfFactory() {
    UUID categoryId = UUID.randomUUID();
    Money amount = Money.of(new BigDecimal("300.00"), EUR);

    Budget budget = Budget.of(categoryId, MONTH, amount);

    assertThat(budget.id()).isNotNull();
    assertThat(budget.categoryId()).isEqualTo(categoryId);
    assertThat(budget.month()).isEqualTo(MONTH);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenIdIsNull() {
    Money amount = Money.of(new BigDecimal("100.00"), EUR);

    assertThatThrownBy(() -> new Budget(null, UUID.randomUUID(), MONTH, amount))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenCategoryIdIsNull() {
    Money amount = Money.of(new BigDecimal("100.00"), EUR);

    assertThatThrownBy(() -> new Budget(UUID.randomUUID(), null, MONTH, amount))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenMonthIsNull() {
    Money amount = Money.of(new BigDecimal("100.00"), EUR);

    assertThatThrownBy(() -> new Budget(UUID.randomUUID(), UUID.randomUUID(), null, amount))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenAmountIsNull() {
    assertThatThrownBy(() -> new Budget(UUID.randomUUID(), UUID.randomUUID(), MONTH, null))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @MethodSource("negativeAmounts")
  void shouldThrowInvalidBudgetExceptionWhenAmountIsNegative(Money negativeAmount) {
    assertThatThrownBy(
            () -> new Budget(UUID.randomUUID(), UUID.randomUUID(), MONTH, negativeAmount))
        .isInstanceOf(InvalidBudgetException.class);
  }

  @Test
  void shouldAllowZeroAmount() {
    Budget budget = new Budget(UUID.randomUUID(), UUID.randomUUID(), MONTH, Money.zero(EUR));

    assertThat(budget.amount().isZero()).isTrue();
  }

  private static Stream<Money> negativeAmounts() {
    return Stream.of(
        Money.of(new BigDecimal("-0.01"), EUR), Money.of(new BigDecimal("-500.00"), EUR));
  }
}
