package com.expensetracker.application.port.out;

/**
 * Fila de un CSV que no se pudo parsear. {@code rowNumber} cuenta desde 1 (cabecera incluida);
 * {@code code} identifica el motivo y {@code reason} lo detalla en texto técnico.
 */
public record RowError(int rowNumber, RowErrorCode code, String reason) {}
