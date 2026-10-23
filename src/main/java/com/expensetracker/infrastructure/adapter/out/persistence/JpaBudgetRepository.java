package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.application.port.out.BudgetRepository;
import com.expensetracker.domain.model.Budget;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida que implementa {@link BudgetRepository} contra PostgreSQL vía JPA.
 *
 * <p>{@code Budget.of(...)} genera un id nuevo en cada llamada aunque conceptualmente se esté
 * sobrescribiendo el presupuesto de una categoría+mes ya existente, y la restricción {@code
 * uq_budgets_category_month} impide insertar una segunda fila para esa combinación. Por eso {@link
 * #save(Budget)} hace el upsert por (categoryId, month) a nivel de fila: si ya existe una fila,
 * reutiliza su id y solo actualiza amount/currency (el id nuevo que trae el {@code Budget} de
 * dominio se descarta); si no existe, inserta una fila nueva con el id del dominio.
 *
 * <p>Entre el {@code findByCategoryIdAndMonth} y el {@code save} hay una carrera real posible (dos
 * {@code PUT} concurrentes para la misma categoría+mes): si ambos ven "no existe" y los dos
 * insertan, la base de datos rechaza el segundo por la restricción única. Se captura esa excepción
 * y se reintenta como actualización en vez de dejarla llegar sin controlar al cliente.
 */
@Repository
public class JpaBudgetRepository implements BudgetRepository {

  private final BudgetJpaRepository jpaRepository;
  private final BudgetMapper mapper;

  public JpaBudgetRepository(BudgetJpaRepository jpaRepository, BudgetMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Budget save(Budget budget) {
    LocalDate month = budget.month().atDay(1);
    try {
      // saveAndFlush (no save): fuerza el INSERT/UPDATE a ejecutarse ya, para que la violación de
      // uq_budgets_category_month se dispare de forma síncrona dentro de este try, igual que en
      // JpaTransactionRepository.
      BudgetEntity entity = buildEntityToSave(budget, month);
      BudgetEntity saved = jpaRepository.saveAndFlush(entity);
      return mapper.toDomain(saved);
    } catch (DataIntegrityViolationException e) {
      if (!UniqueConstraints.isViolationOf(e, UniqueConstraints.BUDGET_CATEGORY_MONTH)) {
        throw e;
      }
      BudgetEntity existing =
          jpaRepository.findByCategoryIdAndMonth(budget.categoryId(), month).orElseThrow(() -> e);
      BudgetEntity saved = jpaRepository.saveAndFlush(applyAmount(existing, budget));
      return mapper.toDomain(saved);
    }
  }

  private BudgetEntity buildEntityToSave(Budget budget, LocalDate month) {
    return jpaRepository
        .findByCategoryIdAndMonth(budget.categoryId(), month)
        .map(existing -> applyAmount(existing, budget))
        .orElseGet(() -> mapper.toEntity(budget));
  }

  private static BudgetEntity applyAmount(BudgetEntity existing, Budget budget) {
    existing.setAmount(budget.amount().amount());
    existing.setCurrency(budget.amount().currency().getCurrencyCode());
    return existing;
  }

  @Override
  public List<Budget> findByMonth(YearMonth month) {
    return jpaRepository.findByMonth(month.atDay(1)).stream().map(mapper::toDomain).toList();
  }

  @Override
  @Transactional
  public boolean delete(UUID categoryId, YearMonth month) {
    return jpaRepository.deleteByCategoryIdAndMonth(categoryId, month.atDay(1)) > 0;
  }
}
