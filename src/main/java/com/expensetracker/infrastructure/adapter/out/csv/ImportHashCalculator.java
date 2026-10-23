package com.expensetracker.infrastructure.adapter.out.csv;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;

/**
 * Calcula el hash de idempotencia de un movimiento: fecha + concepto + importe + índice de
 * duplicado dentro del fichero (necesario para no colapsar dos filas idénticas del mismo CSV en una
 * sola, p. ej. dos cafés de 1,50€ el mismo día).
 */
final class ImportHashCalculator {

  private ImportHashCalculator() {}

  static String calculate(
      LocalDate date, String concept, BigDecimal amount, int duplicateIndexInFile) {
    String raw = date + "|" + concept + "|" + amount.toPlainString() + "|" + duplicateIndexInFile;
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available on this JVM", e);
    }
  }
}
