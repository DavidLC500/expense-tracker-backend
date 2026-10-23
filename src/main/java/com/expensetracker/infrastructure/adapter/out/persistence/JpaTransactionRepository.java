package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.TransactionMonth;
import com.expensetracker.application.port.in.TransactionPage;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.domain.service.TextNormalizer;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de persistencia de {@link TransactionRepository} sobre JPA/PostgreSQL.
 *
 * <p>La importación usa {@link #insertIgnoringDuplicates(List)}: un único lote de {@code INSERT ...
 * ON CONFLICT (import_hash) DO NOTHING} en una transacción. Es idempotente y seguro ante dos
 * importaciones concurrentes del mismo fichero sin consultar antes si cada fila existe, y mucho más
 * rápido que guardar fila a fila (1 MB de CSV son decenas de miles de filas). Reaplicar reglas usa
 * {@link #updateCategories(List)}, también por lotes.
 */
@Repository
public class JpaTransactionRepository implements TransactionRepository {

  private static final char LIKE_ESCAPE = '\\';

  private static final String INSERT_IGNORING_DUPLICATES =
      "INSERT INTO transactions (id, date, concept, amount, currency, category_id, import_hash,"
          + " created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (import_hash) DO NOTHING";

  private static final String UPDATE_CATEGORY =
      "UPDATE transactions SET category_id = ? WHERE id = ?";

  private static final String COUNT_BY_MONTH =
      "SELECT EXTRACT(YEAR FROM date)::int AS year, EXTRACT(MONTH FROM date)::int AS month,"
          + " count(*) AS transactions FROM transactions GROUP BY 1, 2 ORDER BY 1 DESC, 2 DESC";

  // Filas por viaje a la base de datos: suficiente para amortizar la latencia sin un lote gigante.
  private static final int BATCH_SIZE = 500;

  private final TransactionJpaRepository jpaRepository;
  private final TransactionMapper mapper;
  private final JdbcOperations jdbcOperations;

  public JpaTransactionRepository(
      TransactionJpaRepository jpaRepository,
      TransactionMapper mapper,
      JdbcOperations jdbcOperations) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
    this.jdbcOperations = jdbcOperations;
  }

  // saveAndFlush: este adaptador también escribe por JDBC (lotes), y esas sentencias solo ven la
  // fila si JPA ya la ha volcado, aunque todo vaya en la misma transacción.
  @Override
  public Transaction save(Transaction transaction) {
    return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(transaction)));
  }

  @Override
  public Optional<Transaction> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  @Transactional
  public List<Transaction> insertIgnoringDuplicates(List<Transaction> transactions) {
    int[][] counts =
        jdbcOperations.batchUpdate(
            INSERT_IGNORING_DUPLICATES,
            transactions,
            BATCH_SIZE,
            (statement, transaction) -> {
              statement.setObject(1, transaction.id());
              statement.setObject(2, transaction.date());
              statement.setString(3, transaction.concept());
              statement.setBigDecimal(4, transaction.amount().amount());
              statement.setString(5, transaction.amount().currency().getCurrencyCode());
              statement.setObject(6, transaction.categoryId());
              statement.setString(7, transaction.importHash());
              statement.setObject(
                  8, OffsetDateTime.ofInstant(transaction.createdAt(), ZoneOffset.UTC));
            });

    // Una cuenta por fila y en el mismo orden: 1 si se insertó, 0 si ON CONFLICT la descartó.
    List<Transaction> inserted = new ArrayList<>();
    int index = 0;
    for (int[] batch : counts) {
      for (int count : batch) {
        if (count > 0) {
          inserted.add(transactions.get(index));
        }
        index++;
      }
    }
    return inserted;
  }

  @Override
  public List<TransactionMonth> countByMonth() {
    return jdbcOperations.query(
        COUNT_BY_MONTH,
        (row, rowNumber) ->
            new TransactionMonth(
                YearMonth.of(row.getInt("year"), row.getInt("month")),
                row.getLong("transactions")));
  }

  @Override
  @Transactional
  public void updateCategories(List<Transaction> transactions) {
    jdbcOperations.batchUpdate(
        UPDATE_CATEGORY,
        transactions,
        BATCH_SIZE,
        (statement, transaction) -> {
          statement.setObject(1, transaction.categoryId());
          statement.setObject(2, transaction.id());
        });
  }

  @Override
  public List<Transaction> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Transaction> findByMonth(YearMonth month) {
    return jpaRepository.findByDateBetween(month.atDay(1), month.atEndOfMonth()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public TransactionPage findByFilter(TransactionFilter filter) {
    Specification<TransactionEntity> specification = Specification.unrestricted();

    if (filter.month() != null) {
      LocalDate start = filter.month().atDay(1);
      LocalDate end = filter.month().atEndOfMonth();
      specification =
          specification.and((root, query, cb) -> cb.between(root.get("date"), start, end));
    }

    if (filter.categoryId() != null) {
      specification =
          specification.and(
              (root, query, cb) -> cb.equal(root.get("categoryId"), filter.categoryId()));
    }

    if (filter.uncategorized()) {
      specification = specification.and((root, query, cb) -> cb.isNull(root.get("categoryId")));
    }

    if (filter.text() != null && !filter.text().isBlank()) {
      // Ambos lados sin tildes y en mayúsculas: el texto con TextNormalizer y la columna con
      // unaccent() de PostgreSQL (V5__enable_unaccent.sql).
      String pattern = "%" + escapeLikeWildcards(TextNormalizer.normalize(filter.text())) + "%";
      specification =
          specification.and(
              (root, query, cb) ->
                  cb.like(
                      cb.upper(cb.function("unaccent", String.class, root.get("concept"))),
                      pattern,
                      LIKE_ESCAPE));
    }

    PageRequest pageRequest =
        PageRequest.of(
            filter.page(),
            filter.pageSize(),
            Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.ASC, "id")));

    Page<TransactionEntity> page = jpaRepository.findAll(specification, pageRequest);

    List<Transaction> items = page.getContent().stream().map(mapper::toDomain).toList();
    return new TransactionPage(items, filter.page(), filter.pageSize(), page.getTotalElements());
  }

  // Sin escapar, buscar "%" o "_" devolvería todos los movimientos: el usuario busca texto literal.
  private static String escapeLikeWildcards(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
