package com.expensetracker.application.port.in;

import java.util.UUID;

/** Caso de uso: eliminar una regla de categorización existente. */
public interface DeleteRuleUseCase {

  void execute(UUID ruleId);
}
