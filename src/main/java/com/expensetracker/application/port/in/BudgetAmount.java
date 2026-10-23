package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Money;
import java.util.UUID;

/** Importe de presupuesto a asignar a una categoría en {@link SetBudgetsUseCase}. */
public record BudgetAmount(UUID categoryId, Money amount) {}
