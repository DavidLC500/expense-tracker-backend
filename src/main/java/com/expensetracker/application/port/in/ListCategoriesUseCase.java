package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.Category;
import java.util.List;

/** Caso de uso: listar todas las categorías existentes. */
public interface ListCategoriesUseCase {

  List<Category> execute();
}
