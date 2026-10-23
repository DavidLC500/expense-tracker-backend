package com.expensetracker.application.port.in;

/** Resumen del resultado de {@code ImportTransactionsUseCase.execute}. */
public record ImportSummary(int imported, int duplicatesIgnored) {}
