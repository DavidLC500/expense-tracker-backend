package com.expensetracker.application.port.out;

/**
 * Motivo de error de una fila del CSV como código estable: el frontend lo traduce a un mensaje para
 * el usuario, sin depender del texto técnico de {@link RowError#reason()}.
 */
public enum RowErrorCode {
  MISSING_COLUMNS,
  MALFORMED_CSV,
  INVALID_DATE,
  BLANK_CONCEPT,
  CONCEPT_TOO_LONG,
  BLANK_AMOUNT,
  INVALID_AMOUNT,
  AMOUNT_TOO_MANY_DECIMALS,
  AMOUNT_TOO_LARGE,
  INVALID_ROW
}
