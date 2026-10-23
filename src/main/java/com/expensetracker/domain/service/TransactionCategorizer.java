package com.expensetracker.domain.service;

import com.expensetracker.domain.model.Rule;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Asigna una categoría a un concepto de movimiento aplicando las reglas de categorización. */
public final class TransactionCategorizer {

  private TransactionCategorizer() {}

  /**
   * Devuelve la categoría de la regla de menor {@code priority} cuyo patrón esté contenido en el
   * concepto normalizado. En caso de empate de prioridad gana la primera regla en el orden de
   * {@code rules} (no se reordena la lista recibida).
   */
  public static Optional<UUID> categorize(String concept, List<Rule> rules) {
    String normalizedConcept = TextNormalizer.normalize(concept);

    return rules.stream()
        .filter(rule -> normalizedConcept.contains(rule.pattern()))
        .min(Comparator.comparingInt(Rule::priority))
        .map(Rule::categoryId);
  }
}
