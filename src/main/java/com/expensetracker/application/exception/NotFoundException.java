package com.expensetracker.application.exception;

import java.io.Serial;
import java.util.UUID;

/**
 * Base de las excepciones de "no existe ningún X con ese id" (o esa clave). Construye el mensaje en
 * un único sitio y el adaptador web las traduce todas a 404 sin conocer cada subclase.
 */
public abstract class NotFoundException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  protected NotFoundException(String resource, UUID id) {
    this(resource + " not found with id " + id);
  }

  // Para lo que no se identifica por un id propio, como un presupuesto (categoría + mes).
  protected NotFoundException(String message) {
    super(message);
  }
}
