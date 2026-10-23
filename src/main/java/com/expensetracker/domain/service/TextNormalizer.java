package com.expensetracker.domain.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normaliza texto libre (conceptos de movimientos, patrones de reglas) a una forma comparable: sin
 * tildes, en mayúsculas y con espacios colapsados.
 */
public final class TextNormalizer {

  private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
  private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

  private TextNormalizer() {}

  public static String normalize(String input) {
    if (input == null) {
      return "";
    }

    String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
    String withoutDiacritics = DIACRITICS.matcher(decomposed).replaceAll("");
    String trimmedUpperCase = withoutDiacritics.toUpperCase(Locale.ROOT).trim();
    return MULTIPLE_SPACES.matcher(trimmedUpperCase).replaceAll(" ");
  }
}
