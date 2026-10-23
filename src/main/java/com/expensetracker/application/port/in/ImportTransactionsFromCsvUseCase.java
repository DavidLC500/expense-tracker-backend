package com.expensetracker.application.port.in;

import com.expensetracker.application.port.out.BankProfile;
import java.io.InputStream;

/** Caso de uso: importar un CSV de movimientos bancarios usando el perfil de banco indicado. */
public interface ImportTransactionsFromCsvUseCase {

  CsvImportSummary execute(InputStream csvContent, BankProfile profile);
}
