package com.expensetracker.testdata;

import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.TransactionMonth;
import com.expensetracker.application.port.in.TransactionPage;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.domain.service.TextNormalizer;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fake en memoria de {@code TransactionRepository} para tests de casos de uso.
 *
 * <p>{@code findByFilter} ordena los resultados por fecha descendente (más reciente primero) y,
 * para desempatar de forma determinista entre movimientos del mismo día, por id ascendente. La
 * paginación es 0-indexed sobre esa lista ya ordenada y filtrada.
 */
public final class InMemoryTransactionRepository implements TransactionRepository {

  private final Map<UUID, Transaction> transactions = new LinkedHashMap<>();

  @Override
  public Transaction save(Transaction transaction) {
    transactions.put(transaction.id(), transaction);
    return transaction;
  }

  @Override
  public Optional<Transaction> findById(UUID id) {
    return Optional.ofNullable(transactions.get(id));
  }

  @Override
  public List<Transaction> insertIgnoringDuplicates(List<Transaction> incoming) {
    List<Transaction> inserted = new ArrayList<>();
    for (Transaction transaction : incoming) {
      boolean exists =
          transactions.values().stream()
              .anyMatch(existing -> existing.importHash().equals(transaction.importHash()));
      if (!exists) {
        transactions.put(transaction.id(), transaction);
        inserted.add(transaction);
      }
    }
    return inserted;
  }

  @Override
  public void updateCategories(List<Transaction> updated) {
    for (Transaction transaction : updated) {
      Transaction existing = transactions.get(transaction.id());
      if (existing != null) {
        // Como el UPDATE real (solo category_id): el indicador de corrección manual no cambia.
        UUID categoryId = transaction.categoryId();
        transactions.put(
            existing.id(),
            existing.manuallyCategorized()
                ? existing.withManualCategory(categoryId)
                : existing.withRuleCategory(categoryId));
      }
    }
  }

  @Override
  public List<Transaction> findAll() {
    return new ArrayList<>(transactions.values());
  }

  @Override
  public List<Transaction> findByMonth(YearMonth month) {
    return transactions.values().stream()
        .filter(transaction -> YearMonth.from(transaction.date()).equals(month))
        .toList();
  }

  @Override
  public TransactionPage findByFilter(TransactionFilter filter) {
    String normalizedText = filter.text() == null ? null : TextNormalizer.normalize(filter.text());

    List<Transaction> filtered =
        transactions.values().stream()
            .filter(t -> filter.month() == null || YearMonth.from(t.date()).equals(filter.month()))
            .filter(
                t ->
                    filter.categoryId() == null
                        || Objects.equals(t.categoryId(), filter.categoryId()))
            .filter(t -> !filter.uncategorized() || t.isUncategorized())
            .filter(
                t ->
                    normalizedText == null
                        || normalizedText.isBlank()
                        || TextNormalizer.normalize(t.concept()).contains(normalizedText))
            .sorted(
                Comparator.comparing(Transaction::date).reversed().thenComparing(Transaction::id))
            .toList();

    long totalElements = filtered.size();
    int fromIndex = (int) Math.min((long) filter.page() * filter.pageSize(), filtered.size());
    int toIndex = (int) Math.min((long) fromIndex + filter.pageSize(), filtered.size());
    List<Transaction> pageItems = new ArrayList<>(filtered.subList(fromIndex, toIndex));

    return new TransactionPage(pageItems, filter.page(), filter.pageSize(), totalElements);
  }

  @Override
  public List<TransactionMonth> countByMonth() {
    return transactions.values().stream()
        .collect(Collectors.groupingBy(t -> YearMonth.from(t.date()), Collectors.counting()))
        .entrySet()
        .stream()
        .sorted(Map.Entry.<YearMonth, Long>comparingByKey().reversed())
        .map(entry -> new TransactionMonth(entry.getKey(), entry.getValue()))
        .toList();
  }
}
