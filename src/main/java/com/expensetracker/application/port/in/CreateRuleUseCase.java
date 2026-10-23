package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Rule;
import java.util.UUID;

/** Caso de uso: crear una nueva regla de categorización automática. */
public interface CreateRuleUseCase {

  Rule execute(String pattern, UUID categoryId, int priority);
}
