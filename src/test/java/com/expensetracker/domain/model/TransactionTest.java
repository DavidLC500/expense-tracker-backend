package com.expensetracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.domain.exception.InvalidTransactionException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  private static Transaction validTransaction(UUID categoryId) {
    return new Transaction(
        UUID.randomUUID(),
        LocalDate.of(2026, 3, 15),
        "MERCADONA MADRID",
        Money.of(new BigDecimal("-42.50"), EUR),
        categoryId,
        "hash-123",
        Instant.parse("2026-03-15T10:00:00Z"));
  }

  @Test
  void shouldCreateTransactionWithAllFields() {
    UUID id = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    LocalDate date = LocalDate.of(2026, 3, 15);
    Money amount = Money.of(new BigDecimal("-42.50"), EUR);
    Instant createdAt = Instant.parse("2026-03-15T10:00:00Z");

    Transaction transaction =
        new Transaction(id, date, "MERCADONA MADRID", amount, categoryId, "hash-123", createdAt);

    assertThat(transaction.id()).isEqualTo(id);
    assertThat(transaction.date()).isEqualTo(date);
    assertThat(transaction.concept()).isEqualTo("MERCADONA MADRID");
    assertThat(transaction.amount()).isEqualTo(amount);
    assertThat(transaction.categoryId()).isEqualTo(categoryId);
    assertThat(transaction.importHash()).isEqualTo("hash-123");
    assertThat(transaction.createdAt()).isEqualTo(createdAt);
  }

  @Test
  void shouldAllowNullCategoryIdAsUncategorized() {
    Transaction transaction = validTransaction(null);

    assertThat(transaction.categoryId()).isNull();
    assertThat(transaction.isUncategorized()).isTrue();
  }

  @Test
  void shouldNotBeUncategorizedWhenCategoryIdIsPresent() {
    Transaction transaction = validTransaction(UUID.randomUUID());

    assertThat(transaction.isUncategorized()).isFalse();
  }

  @Test
  void shouldThrowNullPointerExceptionWhenIdIsNull() {
    assertThatThrownBy(
            () ->
                new Transaction(
                    null,
                    LocalDate.now(),
                    "CONCEPT",
                    Money.of(BigDecimal.TEN, EUR),
                    null,
                    "hash",
                    Instant.now()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenDateIsNull() {
    assertThatThrownBy(
            () ->
                new Transaction(
                    UUID.randomUUID(),
                    null,
                    "CONCEPT",
                    Money.of(BigDecimal.TEN, EUR),
                    null,
                    "hash",
                    Instant.now()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenAmountIsNull() {
    assertThatThrownBy(
            () ->
                new Transaction(
                    UUID.randomUUID(),
                    LocalDate.now(),
                    "CONCEPT",
                    null,
                    null,
                    "hash",
                    Instant.now()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenImportHashIsNull() {
    assertThatThrownBy(
            () ->
                new Transaction(
                    UUID.randomUUID(),
                    LocalDate.now(),
                    "CONCEPT",
                    Money.of(BigDecimal.TEN, EUR),
                    null,
                    null,
                    Instant.now()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenCreatedAtIsNull() {
    assertThatThrownBy(
            () ->
                new Transaction(
                    UUID.randomUUID(),
                    LocalDate.now(),
                    "CONCEPT",
                    Money.of(BigDecimal.TEN, EUR),
                    null,
                    "hash",
                    null))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void shouldThrowInvalidTransactionExceptionWhenConceptIsNullOrBlank(String blankConcept) {
    assertThatThrownBy(
            () ->
                new Transaction(
                    UUID.randomUUID(),
                    LocalDate.now(),
                    blankConcept,
                    Money.of(BigDecimal.TEN, EUR),
                    null,
                    "hash",
                    Instant.now()))
        .isInstanceOf(InvalidTransactionException.class);
  }

  @Test
  void withRuleCategoryShouldReturnNewTransactionWithUpdatedCategoryKeepingOtherFieldsUnchanged() {
    Transaction original = validTransaction(null);
    UUID newCategoryId = UUID.randomUUID();

    Transaction recategorized = original.withRuleCategory(newCategoryId);

    assertThat(recategorized.categoryId()).isEqualTo(newCategoryId);
    assertThat(recategorized.id()).isEqualTo(original.id());
    assertThat(recategorized.date()).isEqualTo(original.date());
    assertThat(recategorized.concept()).isEqualTo(original.concept());
    assertThat(recategorized.amount()).isEqualTo(original.amount());
    assertThat(recategorized.importHash()).isEqualTo(original.importHash());
    assertThat(recategorized.createdAt()).isEqualTo(original.createdAt());
  }

  @Test
  void withRuleCategoryShouldNotMutateOriginalTransaction() {
    Transaction original = validTransaction(null);

    original.withRuleCategory(UUID.randomUUID());

    assertThat(original.categoryId()).isNull();
  }

  @Test
  void withRuleCategoryShouldAllowClearingCategoryBackToNull() {
    Transaction categorized = validTransaction(UUID.randomUUID());

    Transaction uncategorized = categorized.withRuleCategory(null);

    assertThat(uncategorized.isUncategorized()).isTrue();
  }

  @Test
  void shouldNotBeManuallyCategorizedByDefault() {
    assertThat(validTransaction(UUID.randomUUID()).manuallyCategorized()).isFalse();
  }

  @Test
  void withManualCategoryShouldSetCategoryAndMarkItAsManual() {
    UUID categoryId = UUID.randomUUID();

    Transaction corrected = validTransaction(null).withManualCategory(categoryId);

    assertThat(corrected.categoryId()).isEqualTo(categoryId);
    assertThat(corrected.manuallyCategorized()).isTrue();
  }

  @Test
  void withManualCategoryShouldAllowChoosingNoCategory() {
    Transaction corrected = validTransaction(UUID.randomUUID()).withManualCategory(null);

    assertThat(corrected.isUncategorized()).isTrue();
    assertThat(corrected.manuallyCategorized()).isTrue();
  }

  @Test
  void withRuleCategoryShouldHandTheCategoryBackToTheRules() {
    Transaction manual = validTransaction(null).withManualCategory(UUID.randomUUID());
    UUID ruleCategory = UUID.randomUUID();

    Transaction automatic = manual.withRuleCategory(ruleCategory);

    assertThat(automatic.categoryId()).isEqualTo(ruleCategory);
    assertThat(automatic.manuallyCategorized()).isFalse();
  }

  @Test
  void transactionsWithSameValuesShouldBeEqual() {
    UUID id = UUID.randomUUID();
    LocalDate date = LocalDate.of(2026, 3, 15);
    Money amount = Money.of(new BigDecimal("-42.50"), EUR);
    Instant createdAt = Instant.parse("2026-03-15T10:00:00Z");

    Transaction first =
        new Transaction(id, date, "MERCADONA MADRID", amount, null, "hash-123", createdAt);
    Transaction second =
        new Transaction(id, date, "MERCADONA MADRID", amount, null, "hash-123", createdAt);

    assertThat(first).isEqualTo(second);
    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
