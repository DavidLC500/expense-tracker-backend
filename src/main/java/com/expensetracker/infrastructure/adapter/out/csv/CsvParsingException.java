package com.expensetracker.infrastructure.adapter.out.csv;

import java.io.Serial;

/**
 * Se lanza cuando el fichero CSV no se puede leer en absoluto (stream corrupto, sin cabecera,
 * etc.).
 */
public class CsvParsingException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public CsvParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}
