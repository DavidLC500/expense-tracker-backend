package com.expensetracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TextNormalizerTest {

  @ParameterizedTest
  @NullAndEmptySource
  void shouldReturnEmptyStringForNullOrEmptyInput(String input) {
    assertThat(TextNormalizer.normalize(input)).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
    "Mercadona, MERCADONA",
    "mercadona, MERCADONA",
    "MeRcAdOnA, MERCADONA",
    "Café, CAFE",
    "CAFÉ, CAFE",
    "Alimentación, ALIMENTACION",
    "Días, DIAS",
    "Núñez, NUNEZ"
  })
  void shouldStripDiacriticsAndUpperCase(String input, String expected) {
    assertThat(TextNormalizer.normalize(input)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"Mercadona   Madrid", "Mercadona     Madrid", "Mercadona\tMadrid"})
  void shouldCollapseMultipleWhitespacesIntoOne(String input) {
    assertThat(TextNormalizer.normalize(input)).isEqualTo("MERCADONA MADRID");
  }

  @Test
  void shouldTrimLeadingAndTrailingWhitespace() {
    assertThat(TextNormalizer.normalize("   Mercadona Madrid   ")).isEqualTo("MERCADONA MADRID");
  }

  @Test
  void shouldNormalizeNEneWithTildeToPlainN() {
    // Decision de diseno: NFD descompone "Ñ" en "N" + marca de combinacion (tilde), que
    // \p{M} elimina como cualquier otro diacritico. El resultado es que "Ñ" se normaliza a
    // "N", no se preserva como letra distinta. Es una simplificacion deliberada: unifica
    // "NIÑO" y "NINO" para que coincidan patrones de reglas escritos sin eñe.
    assertThat(TextNormalizer.normalize("Niño")).isEqualTo("NINO");
  }

  @Test
  void shouldReturnSameResultForAlreadyNormalizedInput() {
    assertThat(TextNormalizer.normalize("MERCADONA MADRID")).isEqualTo("MERCADONA MADRID");
  }
}
