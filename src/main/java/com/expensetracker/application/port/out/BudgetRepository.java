package com.expensetracker.application.port.out;

import com.expensetracker.domain.model.Budget;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Puerto de salida para la persistencia de presupuestos mensuales por categoría. */
public interface BudgetRepository {

  /** Upsert: si ya existe un {@code Budget} para (categoryId, month), lo reemplaza. */
  Budget save(Budget budget);

  List<Budget> findByMonth(YearMonth month);

  /** Borra el presupuesto de esa categoría y mes. Devuelve {@code false} si no había ninguno. */
  boolean delete(UUID categoryId, YearMonth month);
}
