package com.expensetracker.application.port.in;

import java.time.YearMonth;
import java.util.UUID;

/**
 * Filtro para listar movimientos. {@code month}, {@code categoryId} y {@code text} son nullable
 * (null = sin ese filtro). {@code text} filtra por {@code Transaction.concept()} conteniendo el
 * texto, de forma insensible a mayúsculas/minúsculas y tildes (misma normalización que {@code
 * TextNormalizer} del dominio). {@code uncategorized} limita a los movimientos sin categoría y no
 * se combina con {@code categoryId}.
 */
public record TransactionFilter(
    YearMonth month, UUID categoryId, boolean uncategorized, String text, int page, int pageSize) {

  // Sin tope, un cliente podía pedir pageSize=1000000 y cargar toda la tabla en una petición.
  public static final int MAX_PAGE_SIZE = 100;

  public TransactionFilter(YearMonth month, UUID categoryId, String text, int page, int pageSize) {
    this(month, categoryId, false, text, page, pageSize);
  }

  public TransactionFilter {
    if (uncategorized && categoryId != null) {
      throw new IllegalArgumentException("categoryId and uncategorized cannot be combined");
    }
    if (page < 0) {
      throw new IllegalArgumentException("page must be >= 0");
    }
    if (pageSize <= 0) {
      throw new IllegalArgumentException("pageSize must be > 0");
    }
    if (pageSize > MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("pageSize must be <= " + MAX_PAGE_SIZE);
    }
  }
}
