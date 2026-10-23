package com.expensetracker.application.port.out;

import java.io.InputStream;

/** Puerto de salida para el parseo de CSV de movimientos bancarios. */
public interface CsvTransactionParser {

  CsvParseOutcome parse(InputStream csvContent, BankProfile profile);
}
