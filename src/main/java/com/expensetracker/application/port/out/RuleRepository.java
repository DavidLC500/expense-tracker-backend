package com.expensetracker.application.port.out;

import com.expensetracker.domain.model.Rule;
import java.util.List;
import java.util.UUID;

/** Puerto de salida para la persistencia de reglas de categorización. */
public interface RuleRepository {

  Rule save(Rule rule);

  /**
   * Devuelve las reglas ordenadas por prioridad, patrón e id. El orden debe ser estable porque
   * {@code TransactionCategorizer} resuelve los empates de prioridad por posición en la lista.
   */
  List<Rule> findAll();

  boolean existsById(UUID id);

  /**
   * No-op si no existe un {@code Rule} con ese id; la validación de existencia es del caso de uso
   * llamador.
   */
  void deleteById(UUID id);
}
