package com.expensetracker.testdata;

import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.domain.model.Rule;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Fake en memoria de {@code RuleRepository} para tests de casos de uso. */
public final class InMemoryRuleRepository implements RuleRepository {

  private final Map<UUID, Rule> rules = new LinkedHashMap<>();

  @Override
  public Rule save(Rule rule) {
    rules.put(rule.id(), rule);
    return rule;
  }

  @Override
  public List<Rule> findAll() {
    return rules.values().stream()
        .sorted(
            Comparator.comparingInt(Rule::priority)
                .thenComparing(Rule::pattern)
                .thenComparing(Rule::id))
        .toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return rules.containsKey(id);
  }

  @Override
  public void deleteById(UUID id) {
    rules.remove(id);
  }
}
