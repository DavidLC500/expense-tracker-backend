package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.TransactionPage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Página de movimientos devuelta por {@code GET /api/transactions}. */
@Schema(description = "Página de movimientos.")
public record TransactionPageResponse(
    @Schema(description = "Movimientos de esta página.") List<TransactionResponse> items,
    @Schema(description = "Página devuelta, empezando en 0.", example = "0") int page,
    @Schema(description = "Movimientos por página.", example = "20") int pageSize,
    @Schema(description = "Total de movimientos que cumplen los filtros.", example = "7")
        long totalElements) {

  public TransactionPageResponse {
    items = List.copyOf(items);
  }

  static TransactionPageResponse from(TransactionPage page) {
    return new TransactionPageResponse(
        page.items().stream().map(TransactionResponse::from).toList(),
        page.page(),
        page.pageSize(),
        page.totalElements());
  }
}
