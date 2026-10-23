package com.expensetracker.application.port.in;

import com.expensetracker.application.port.out.RowError;
import java.util.List;

/**
 * Resumen de {@code ImportTransactionsFromCsvUseCase.execute}: importados, duplicados y filas con
 * error.
 */
public record CsvImportSummary(int imported, int duplicatesIgnored, List<RowError> rowErrors) {

  public CsvImportSummary {
    rowErrors = List.copyOf(rowErrors);
  }
}
