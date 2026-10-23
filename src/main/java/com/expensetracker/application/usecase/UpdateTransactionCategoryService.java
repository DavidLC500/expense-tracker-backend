package com.expensetracker.application.usecase;

import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.application.exception.TransactionNotFoundException;
import com.expensetracker.application.port.in.UpdateTransactionCategoryUseCase;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Transaction;
import java.util.Objects;
import java.util.UUID;

public final class UpdateTransactionCategoryService implements UpdateTransactionCategoryUseCase {

  private final TransactionRepository transactionRepository;
  private final CategoryRepository categoryRepository;

  public UpdateTransactionCategoryService(
      TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
    this.categoryRepository =
        Objects.requireNonNull(categoryRepository, "categoryRepository must not be null");
  }

  @Override
  public Transaction execute(UUID transactionId, UUID newCategoryId) {
    Transaction transaction =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));

    // null es válido: deja el movimiento sin categoría.
    if (newCategoryId != null && !categoryRepository.existsById(newCategoryId)) {
      throw new CategoryNotFoundException(newCategoryId);
    }

    return transactionRepository.save(transaction.withManualCategory(newCategoryId));
  }
}
