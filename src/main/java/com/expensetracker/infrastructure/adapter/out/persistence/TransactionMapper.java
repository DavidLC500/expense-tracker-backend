package com.expensetracker.infrastructure.adapter.out.persistence;

import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.Transaction;
import java.util.Currency;
import org.mapstruct.Mapper;

/**
 * Mapeo entre {@link Transaction} (dominio) y {@link TransactionEntity} (JPA).
 *
 * <p>{@code Money} no mapea 1:1 a un campo de la entidad (se parte en {@code amount} + {@code
 * currency}), por lo que no se usa el mapeo automático de MapStruct: los métodos son {@code
 * default} explícitos.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

  default Transaction toDomain(TransactionEntity entity) {
    return new Transaction(
        entity.getId(),
        entity.getDate(),
        entity.getConcept(),
        Money.of(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
        entity.getCategoryId(),
        entity.getImportHash(),
        entity.getCreatedAt(),
        entity.isManuallyCategorized());
  }

  default TransactionEntity toEntity(Transaction transaction) {
    TransactionEntity entity = new TransactionEntity();
    entity.setId(transaction.id());
    entity.setDate(transaction.date());
    entity.setConcept(transaction.concept());
    entity.setAmount(transaction.amount().amount());
    entity.setCurrency(transaction.amount().currency().getCurrencyCode());
    entity.setCategoryId(transaction.categoryId());
    entity.setImportHash(transaction.importHash());
    entity.setCreatedAt(transaction.createdAt());
    entity.setManuallyCategorized(transaction.manuallyCategorized());
    return entity;
  }
}
