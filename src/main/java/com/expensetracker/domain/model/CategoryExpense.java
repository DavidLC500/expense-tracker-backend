package com.expensetracker.domain.model;

import java.util.UUID;

/**
 * Gasto total acumulado de una categoría dentro de un período.
 *
 * <p>{@code categoryId} es {@code null} para agrupar las transacciones sin categoría ("Sin
 * categoría"), igual que {@link Transaction#categoryId()}.
 */
public record CategoryExpense(UUID categoryId, Money total) {}
