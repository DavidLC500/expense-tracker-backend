package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Category;

/** Caso de uso: crear una nueva categoría de gasto o ingreso. */
public interface CreateCategoryUseCase {

  Category execute(String name);
}
