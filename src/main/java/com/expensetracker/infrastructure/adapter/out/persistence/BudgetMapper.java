package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Money;
import java.time.YearMonth;
import java.util.Currency;
import org.mapstruct.Mapper;

/**
 * Mapeo entre {@link Budget} (dominio) y {@link BudgetEntity} (JPA).
 *
 * <p>{@link Money} y {@link YearMonth} no mapean 1:1 a las columnas de la entidad (importe+divisa
 * separados, mes como {@code LocalDate} del primer día), así que el mapeo se escribe a mano en vez
 * de dejarlo a MapStruct.
 */
@Mapper(componentModel = "spring")
public interface BudgetMapper {

  default Budget toDomain(BudgetEntity entity) {
    return new Budget(
        entity.getId(),
        entity.getCategoryId(),
        YearMonth.from(entity.getMonth()),
        Money.of(entity.getAmount(), Currency.getInstance(entity.getCurrency())));
  }

  default BudgetEntity toEntity(Budget budget) {
    BudgetEntity entity = new BudgetEntity();
    entity.setId(budget.id());
    entity.setCategoryId(budget.categoryId());
    entity.setMonth(budget.month().atDay(1));
    entity.setAmount(budget.amount().amount());
    entity.setCurrency(budget.amount().currency().getCurrencyCode());
    return entity;
  }
}
