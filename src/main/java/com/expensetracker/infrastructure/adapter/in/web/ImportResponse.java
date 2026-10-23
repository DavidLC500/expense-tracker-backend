package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.CsvImportSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Respuesta de {@code POST /api/transactions/import}: resumen de importados/duplicados/errores. */
@Schema(description = "Resumen de una importación.")
public record ImportResponse(
    @Schema(description = "Movimientos nuevos guardados.", example = "7") int imported,
    @Schema(description = "Movimientos que ya existían y no se han duplicado.", example = "0")
        int duplicatesIgnored,
    @Schema(description = "Filas que no se han podido importar; el resto sí se ha importado.")
        List<RowErrorResponse> errors) {

  public ImportResponse {
    errors = List.copyOf(errors);
  }

  static ImportResponse from(CsvImportSummary summary) {
    return new ImportResponse(
        summary.imported(),
        summary.duplicatesIgnored(),
        summary.rowErrors().stream().map(RowErrorResponse::from).toList());
  }
}
