package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.out.RowError;
import com.expensetracker.application.port.out.RowErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Fila del CSV que no se pudo importar: número de fila (cabecera incluida), código del motivo para
 * traducirlo en el cliente y detalle técnico.
 */
@Schema(description = "Fila del CSV que no se ha podido importar.")
public record RowErrorResponse(
    @Schema(description = "Número de fila contando la cabecera como la 1.", example = "4") int row,
    @Schema(
            description =
                """
                Motivo, como código estable para que el cliente lo traduzca: \
                `MISSING_COLUMNS` (faltan columnas en la cabecera; fila 1), `MALFORMED_CSV` \
                (p. ej. una comilla sin cerrar; las filas siguientes no se importan), \
                `INVALID_DATE`, `BLANK_CONCEPT`, `CONCEPT_TOO_LONG` (más de 500 caracteres), \
                `BLANK_AMOUNT`, `INVALID_AMOUNT`, `AMOUNT_TOO_MANY_DECIMALS`, \
                `AMOUNT_TOO_LARGE` (más de 12 cifras enteras) o `INVALID_ROW` (p. ej. faltan \
                columnas en esa fila).""",
            example = "INVALID_DATE")
        RowErrorCode code,
    @Schema(
            description = "Detalle técnico en inglés.",
            example = "Text '2026-02-30' could not be parsed: Invalid date 'FEBRUARY 30'")
        String reason) {

  static RowErrorResponse from(RowError error) {
    return new RowErrorResponse(error.rowNumber(), error.code(), error.reason());
  }
}
