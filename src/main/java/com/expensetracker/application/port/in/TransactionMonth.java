package com.expensetracker.application.port.in;

import java.time.YearMonth;
import java.util.Objects;

/** Mes con movimientos y cuántos tiene. */
public record TransactionMonth(YearMonth month, long transactions) {

  public TransactionMonth {
    Objects.requireNonNull(month, "month must not be null");
  }
}
