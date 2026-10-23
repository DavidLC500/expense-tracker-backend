package com.expensetracker.application.port.out;

/**
 * Describe el formato de un CSV bancario: qué columnas usar, en qué formato viene la fecha y qué
 * carácter separa los decimales del importe.
 *
 * <p>{@code datePattern} se aplica en modo estricto (una fecha imposible como el 30 de febrero es
 * un error, no se corrige al último día del mes), así que el año se escribe {@code uuuu}: con
 * {@code yyyy} (año de la era) el modo estricto no puede resolver ninguna fecha.
 */
public record BankProfile(
    String name,
    String dateColumn,
    String conceptColumn,
    String amountColumn,
    String datePattern,
    char decimalSeparator) {

  public static BankProfile generic() {
    return new BankProfile("generic", "fecha", "concepto", "importe", "uuuu-MM-dd", '.');
  }
}
