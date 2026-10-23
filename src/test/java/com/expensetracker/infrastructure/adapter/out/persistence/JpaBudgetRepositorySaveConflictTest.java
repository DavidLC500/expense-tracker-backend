package com.expensetracker.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Reproduce de forma determinista (sin hilos reales, que harían el test inestable) la carrera
 * documentada en {@link JpaBudgetRepository}: {@code findByCategoryIdAndMonth} no encuentra fila
 * (otra transacción aún no ha hecho commit), pero el {@code saveAndFlush} choca con {@code
 * uq_budgets_category_month} porque esa otra transacción ya hizo commit justo después del check.
 */
class JpaBudgetRepositorySaveConflictTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  private final BudgetJpaRepository jpaRepository = mock(BudgetJpaRepository.class);
  private final BudgetMapper mapper = new BudgetMapperImpl();
  private final JpaBudgetRepository repository = new JpaBudgetRepository(jpaRepository, mapper);

  @Test
  void shouldRetryAsUpdateWhenSaveAndFlushConflictsOnCategoryAndMonth() {
    UUID categoryId = UUID.randomUUID();
    YearMonth month = YearMonth.of(2026, 9);
    LocalDate monthStart = month.atDay(1);
    Budget incoming = Budget.of(categoryId, month, Money.of(new BigDecimal("200.00"), EUR));

    BudgetEntity existingEntity = new BudgetEntity();
    existingEntity.setId(UUID.randomUUID());
    existingEntity.setCategoryId(categoryId);
    existingEntity.setMonth(monthStart);
    existingEntity.setAmount(new BigDecimal("100.00"));
    existingEntity.setCurrency("EUR");

    BudgetEntity updatedEntity = new BudgetEntity();
    updatedEntity.setId(existingEntity.getId());
    updatedEntity.setCategoryId(categoryId);
    updatedEntity.setMonth(monthStart);
    updatedEntity.setAmount(new BigDecimal("200.00"));
    updatedEntity.setCurrency("EUR");

    DataIntegrityViolationException raceConflict =
        new DataIntegrityViolationException(
            "could not execute statement",
            new RuntimeException(
                "ERROR: duplicate key value violates unique constraint"
                    + " \"uq_budgets_category_month\""));

    given(jpaRepository.findByCategoryIdAndMonth(categoryId, monthStart))
        .willReturn(Optional.empty()) // pre-check del save: "no existe todavia"
        .willReturn(Optional.of(existingEntity)); // reintento tras el conflicto: ya existe
    given(jpaRepository.saveAndFlush(any()))
        .willThrow(raceConflict) // primer intento: pierde la carrera
        .willReturn(updatedEntity); // reintento como actualizacion: funciona

    Budget result = repository.save(incoming);

    assertThat(result.id()).isEqualTo(existingEntity.getId());
    assertThat(result.amount().amount()).isEqualByComparingTo("200.00");
    verify(jpaRepository, times(2)).findByCategoryIdAndMonth(categoryId, monthStart);
    verify(jpaRepository, times(2)).saveAndFlush(any());
  }

  @Test
  void shouldRethrowWhenConflictIsNotTheCategoryMonthUniqueConstraint() {
    UUID categoryId = UUID.randomUUID();
    YearMonth month = YearMonth.of(2026, 9);
    Budget incoming = Budget.of(categoryId, month, Money.of(new BigDecimal("200.00"), EUR));

    DataIntegrityViolationException unrelatedConflict =
        new DataIntegrityViolationException(
            "could not execute statement",
            new RuntimeException(
                "ERROR: null value in column \"amount\" violates not-null constraint"));

    given(jpaRepository.findByCategoryIdAndMonth(categoryId, month.atDay(1)))
        .willReturn(Optional.empty());
    given(jpaRepository.saveAndFlush(any())).willThrow(unrelatedConflict);

    assertThatThrownBy(() -> repository.save(incoming)).isSameAs(unrelatedConflict);
  }
}
