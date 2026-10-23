package com.expensetracker.application.usecase;

import com.expensetracker.application.exception.TransactionNotFoundException;
import com.expensetracker.application.port.in.ResetTransactionCategoryUseCase;
import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.domain.service.TransactionCategorizer;
import java.util.Objects;
import java.util.UUID;

public final class ResetTransactionCategoryService implements ResetTransactionCategoryUseCase {

  private final TransactionRepository transactionRepository;
  private final RuleRepository ruleRepository;

  public ResetTransactionCategoryService(
      TransactionRepository transactionRepository, RuleRepository ruleRepository) {
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
    this.ruleRepository = Objects.requireNonNull(ruleRepository, "ruleRepository must not be null");
  }

  // Se recalcula ya con las reglas vigentes, como si se acabara de importar: así el usuario ve la
  // categoría resultante sin tener que reaplicar las reglas a todos los movimientos.
  @Override
  public Transaction execute(UUID transactionId) {
    Transaction transaction =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    UUID ruleCategory =
        TransactionCategorizer.categorize(transaction.concept(), ruleRepository.findAll())
            .orElse(null);
    return transactionRepository.save(transaction.withRuleCategory(ruleCategory));
  }
}
