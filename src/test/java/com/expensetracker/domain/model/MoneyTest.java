package com.expensetracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.domain.exception.CurrencyMismatchException;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private static final Currency USD = Currency.getInstance("USD");

  @Test
  void shouldThrowNullPointerExceptionWhenAmountIsNull() {
    assertThatThrownBy(() -> new Money(null, EUR)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenCurrencyIsNull() {
    assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldRescaleAmountToCurrencyDefaultFractionDigitsWhenInputHasFewerDecimals() {
    Money money = Money.of(new BigDecimal("10"), EUR);

    assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.00"));
    assertThat(money.amount().scale()).isEqualTo(2);
  }

  @Test
  void shouldRoundNormallyWhenDigitBeyondScaleIsNotATie() {
    Money money = Money.of(new BigDecimal("10.129"), EUR);

    assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.13"));
  }

  @ParameterizedTest(name = "{0} EUR rounds to {1}")
  @CsvSource({
    "10.005, 10.00",
    "10.015, 10.02",
    "-10.005, -10.00",
    "-10.015, -10.02",
  })
  void shouldRoundHalfEvenWhenAmountIsExactlyBetweenTwoCents(String rawAmount, String expected) {
    Money money = Money.of(new BigDecimal(rawAmount), EUR);

    assertThat(money.amount()).isEqualByComparingTo(new BigDecimal(expected));
  }

  @ParameterizedTest(name = "{0} JPY rounds to {1}")
  @CsvSource({
    "10.5, 10",
    "11.5, 12",
  })
  void shouldRoundHalfEvenWhenCurrencyHasZeroFractionDigits(String rawAmount, String expected) {
    Currency jpy = Currency.getInstance("JPY");

    Money money = Money.of(new BigDecimal(rawAmount), jpy);

    assertThat(money.amount()).isEqualByComparingTo(new BigDecimal(expected));
  }

  @Test
  void shouldCreateZeroMoneyForGivenCurrency() {
    Money money = Money.zero(EUR);

    assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(money.isZero()).isTrue();
  }

  @Test
  void shouldAddMoneyWithSameCurrency() {
    Money a = Money.of(new BigDecimal("10.50"), EUR);
    Money b = Money.of(new BigDecimal("2.30"), EUR);

    Money result = a.add(b);

    assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("12.80"));
    assertThat(result.currency()).isEqualTo(EUR);
  }

  @Test
  void shouldSubtractMoneyWithSameCurrency() {
    Money a = Money.of(new BigDecimal("10.50"), EUR);
    Money b = Money.of(new BigDecimal("2.30"), EUR);

    Money result = a.subtract(b);

    assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("8.20"));
    assertThat(result.currency()).isEqualTo(EUR);
  }

  @Test
  void shouldThrowCurrencyMismatchExceptionWhenAddingDifferentCurrencies() {
    Money eur = Money.of(BigDecimal.TEN, EUR);
    Money usd = Money.of(BigDecimal.TEN, USD);

    assertThatThrownBy(() -> eur.add(usd))
        .isInstanceOf(CurrencyMismatchException.class)
        .hasMessageContaining("EUR")
        .hasMessageContaining("USD");
  }

  @Test
  void shouldThrowCurrencyMismatchExceptionWhenSubtractingDifferentCurrencies() {
    Money eur = Money.of(BigDecimal.TEN, EUR);
    Money usd = Money.of(BigDecimal.TEN, USD);

    assertThatThrownBy(() -> eur.subtract(usd)).isInstanceOf(CurrencyMismatchException.class);
  }

  @Test
  void shouldThrowNullPointerExceptionWhenAddingNullMoney() {
    Money eur = Money.of(BigDecimal.TEN, EUR);

    assertThatThrownBy(() -> eur.add(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldNegateAmountPreservingCurrency() {
    Money money = Money.of(new BigDecimal("10.00"), EUR);

    Money negated = money.negate();

    assertThat(negated.amount()).isEqualByComparingTo(new BigDecimal("-10.00"));
    assertThat(negated.currency()).isEqualTo(EUR);
  }

  @ParameterizedTest(name = "{0} -> negative={1}, positive={2}, zero={3}")
  @CsvSource({
    "-5.00, true, false, false",
    "5.00, false, true, false",
    "0.00, false, false, true",
  })
  void shouldReportSignCorrectly(
      String rawAmount, boolean expectedNegative, boolean expectedPositive, boolean expectedZero) {
    Money money = Money.of(new BigDecimal(rawAmount), EUR);

    assertThat(money.isNegative()).isEqualTo(expectedNegative);
    assertThat(money.isPositive()).isEqualTo(expectedPositive);
    assertThat(money.isZero()).isEqualTo(expectedZero);
  }
}
