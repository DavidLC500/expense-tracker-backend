package com.expensetracker.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Estado de un presupuesto de categoría para un mes concreto, resultado de {@code
 * BudgetCalculator}.
 */
public record BudgetStatus(
    UUID categoryId,
    YearMonth month,
    Money budgeted,
    Money spent,
    Money remaining,
    BigDecimal percentageUsed,
    boolean exceeded) {}
