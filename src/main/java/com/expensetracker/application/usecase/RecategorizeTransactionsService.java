package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.RecategorizeTransactionsUseCase;
import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.domain.service.TransactionCategorizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RecategorizeTransactionsService implements RecategorizeTransactionsUseCase {

  private final TransactionRepository transactionRepository;
  private final RuleRepository ruleRepository;

  public RecategorizeTransactionsService(
      TransactionRepository transactionRepository, RuleRepository ruleRepository) {
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
    this.ruleRepository = Objects.requireNonNull(ruleRepository, "ruleRepository must not be null");
  }

  @Override
  public int execute() {
    List<Rule> rules = ruleRepository.findAll();
    List<Transaction> changed = new ArrayList<>();

    for (Transaction transaction : transactionRepository.findAll()) {
      // Una corrección manual manda sobre las reglas.
      if (transaction.manuallyCategorized()) {
        continue;
      }
      UUID newCategoryId =
          TransactionCategorizer.categorize(transaction.concept(), rules).orElse(null);
      if (!Objects.equals(newCategoryId, transaction.categoryId())) {
        changed.add(transaction.withRuleCategory(newCategoryId));
      }
    }

    // Un lote y no un save por fila: con 100.000 movimientos pasa de decenas de segundos a pocos.
    transactionRepository.updateCategories(changed);
    return changed.size();
  }
}
