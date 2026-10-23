package com.expensetracker.testdata;

import com.expensetracker.application.exception.CategoryAlreadyExistsException;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.service.TextNormalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Fake en memoria de {@code CategoryRepository} para tests de casos de uso. Cumple el mismo
 * contrato que el adaptador real: rechaza un nombre repetido sin distinguir mayúsculas ni tildes.
 */
public final class InMemoryCategoryRepository implements CategoryRepository {

  private final Map<UUID, Category> categories = new LinkedHashMap<>();

  @Override
  public Category save(Category category) {
    String normalizedName = TextNormalizer.normalize(category.name());
    boolean duplicate =
        categories.values().stream()
            .anyMatch(
                existing ->
                    !existing.id().equals(category.id())
                        && TextNormalizer.normalize(existing.name()).equals(normalizedName));
    if (duplicate) {
      throw new CategoryAlreadyExistsException("Category already exists: " + category.name());
    }
    categories.put(category.id(), category);
    return category;
  }

  @Override
  public Optional<Category> findById(UUID id) {
    return Optional.ofNullable(categories.get(id));
  }

  @Override
  public List<Category> findAll() {
    return categories.values().stream()
        .sorted(Comparator.comparing(Category::name).thenComparing(Category::id))
        .toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return categories.containsKey(id);
  }
}
