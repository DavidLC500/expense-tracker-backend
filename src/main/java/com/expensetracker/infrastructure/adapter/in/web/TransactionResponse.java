package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.domain.model.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta JSON de un movimiento. {@code categoryId} es {@code null} si está sin categorizar;
 * {@code manuallyCategorized} indica que el usuario la corrigió y las reglas no la cambian.
 */
@Schema(description = "Movimiento bancario.")
public record TransactionResponse(
    @Schema(description = "Id.", example = "5f2c9d3e-8a41-4b7f-a6c2-1d9e0f3b4a85") UUID id,
    @Schema(description = "Fecha del movimiento.", example = "2026-01-05") LocalDate date,
    @Schema(description = "Concepto tal como venía en el CSV.", example = "MERCADONA MADRID")
        String concept,
    @Schema(description = "Importe; negativo es gasto y positivo, ingreso.", example = "-45.30")
        BigDecimal amount,
    @Schema(description = "Divisa (ISO 4217).", example = "EUR") String currency,
    @Schema(
            description = "Categoría; null si no tiene.",
            example = "0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70")
        UUID categoryId,
    @Schema(
            description =
                "Huella del movimiento (SHA-256 de fecha, concepto, importe y repetición dentro"
                    + " del fichero) que hace idempotente la importación.",
            example = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        String importHash,
    @Schema(description = "Cuándo se importó.", example = "2026-02-01T09:30:00Z") Instant createdAt,
    @Schema(
            description =
                "true si el usuario eligió la categoría a mano: al reaplicar las reglas no cambia.",
            example = "false")
        boolean manuallyCategorized) {

  static TransactionResponse from(Transaction transaction) {
    return new TransactionResponse(
        transaction.id(),
        transaction.date(),
        transaction.concept(),
        transaction.amount().amount(),
        transaction.amount().currency().getCurrencyCode(),
        transaction.categoryId(),
        transaction.importHash(),
        transaction.createdAt(),
        transaction.manuallyCategorized());
  }
}
