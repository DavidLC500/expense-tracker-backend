package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Rule;
import java.util.List;

/** Caso de uso: listar todas las reglas de categorización existentes. */
public interface ListRulesUseCase {

  List<Rule> execute();
}
