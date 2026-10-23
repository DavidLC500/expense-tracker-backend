package com.expensetracker.application.port.out;

import com.expensetracker.application.exception.CategoryAlreadyExistsException;
import com.expensetracker.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida para la persistencia de categorías. */
public interface CategoryRepository {

  /**
   * Guarda una categoría nueva. Lanza {@link CategoryAlreadyExistsException} si ya existe otra con
   * el mismo nombre sin distinguir mayúsculas, tildes ni espacios de más (misma normalización que
   * {@code TextNormalizer}), también si se crea a la vez desde otra petición.
   */
  Category save(Category category);

  Optional<Category> findById(UUID id);

  /**
   * Devuelve las categorías ordenadas por nombre, como se muestran en los listados y selectores.
   */
  List<Category> findAll();

  boolean existsById(UUID id);
}
