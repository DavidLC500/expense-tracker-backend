package com.expensetracker.application.port.out;

import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.TransactionMonth;
import com.expensetracker.application.port.in.TransactionPage;
import com.expensetracker.domain.model.Transaction;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida para la persistencia de movimientos bancarios. */
public interface TransactionRepository {

  Transaction save(Transaction transaction);

  Optional<Transaction> findById(UUID id);

  /**
   * Inserta los movimientos cuyo {@code importHash} no exista todavía e ignora el resto, también si
   * otra importación concurrente los inserta a la vez. Devuelve solo los que se han insertado.
   */
  List<Transaction> insertIgnoringDuplicates(List<Transaction> transactions);

  /**
   * Guarda en un único lote la categoría de estos movimientos, que ya deben existir. Solo actualiza
   * {@code categoryId}: es para reaplicar reglas, no para correcciones manuales.
   */
  void updateCategories(List<Transaction> transactions);

  List<Transaction> findAll();

  List<Transaction> findByMonth(YearMonth month);

  TransactionPage findByFilter(TransactionFilter filter);

  /**
   * Meses que tienen al menos un movimiento, del más reciente al más antiguo, con cuántos tiene
   * cada uno.
   */
  List<TransactionMonth> countByMonth();
}
