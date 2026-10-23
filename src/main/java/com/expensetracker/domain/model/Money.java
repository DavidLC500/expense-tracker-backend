package com.expensetracker.domain.model;

import com.expensetracker.domain.exception.CurrencyMismatchException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Importe monetario inmutable ligado a una divisa.
 *
 * <p>El signo sigue la convención de un extracto bancario: negativo es gasto, positivo es ingreso.
 * El importe se reescala siempre al número de decimales que define la divisa (2 para EUR, 0 para
 * JPY, etc.) usando {@link RoundingMode#HALF_EVEN} ("redondeo bancario"): evita el sesgo
 * acumulativo que introduciría redondear siempre hacia arriba en series largas de transacciones.
 */
public record Money(BigDecimal amount, Currency currency) {

  /**
   * Divisa de toda la aplicación: v1 es mono-divisa (varias divisas está en el roadmap del README),
   * sin conversión entre divisas.
   */
  public static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

  public Money {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(currency, "currency must not be null");
    amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
  }

  public static Money zero(Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public static Money of(BigDecimal amount, Currency currency) {
    return new Money(amount, currency);
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money negate() {
    return new Money(amount.negate(), currency);
  }

  public boolean isNegative() {
    return amount.signum() < 0;
  }

  public boolean isPositive() {
    return amount.signum() > 0;
  }

  public boolean isZero() {
    return amount.signum() == 0;
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other, "other must not be null");
    if (!currency.equals(other.currency)) {
      throw new CurrencyMismatchException(currency, other.currency);
    }
  }
}
