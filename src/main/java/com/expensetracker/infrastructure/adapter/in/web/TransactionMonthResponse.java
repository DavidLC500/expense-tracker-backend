package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.TransactionMonth;
import io.swagger.v3.oas.annotations.media.Schema;

/** Elemento de {@code GET /api/transactions/months}. */
@Schema(description = "Mes con movimientos.")
public record TransactionMonthResponse(
    @Schema(description = "Año.", example = "2026") int year,
    @Schema(description = "Mes (1-12).", example = "10") int month,
    @Schema(description = "Movimientos de ese mes.", example = "7") long count) {

  static TransactionMonthResponse from(TransactionMonth transactionMonth) {
    return new TransactionMonthResponse(
        transactionMonth.month().getYear(),
        transactionMonth.month().getMonthValue(),
        transactionMonth.transactions());
  }
}
