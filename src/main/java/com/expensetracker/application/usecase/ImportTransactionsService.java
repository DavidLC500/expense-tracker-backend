package com.expensetracker.application.usecase;

import com.expensetracker.application.port.in.ImportSummary;
import com.expensetracker.application.port.in.ImportTransactionsUseCase;
import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.domain.model.Rule;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.domain.service.TransactionCategorizer;
import java.util.List;
import java.util.Objects;

public final class ImportTransactionsService implements ImportTransactionsUseCase {

  private final TransactionRepository transactionRepository;
  private final RuleRepository ruleRepository;

  public ImportTransactionsService(
      TransactionRepository transactionRepository, RuleRepository ruleRepository) {
    this.transactionRepository =
        Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
    this.ruleRepository = Objects.requireNonNull(ruleRepository, "ruleRepository must not be null");
  }

  @Override
  public ImportSummary execute(List<Transaction> transactions) {
    List<Rule> rules = ruleRepository.findAll();
    List<Transaction> categorized =
        transactions.stream()
            .map(
                transaction ->
                    transaction.withRuleCategory(
                        TransactionCategorizer.categorize(transaction.concept(), rules)
                            .orElse(null)))
            .toList();

    // La idempotencia la resuelve el repositorio: lo que ya existe (por importHash) no se inserta,
    // así que lo no insertado son duplicados, incluidos los que otra importación metió a la vez.
    int inserted = transactionRepository.insertIgnoringDuplicates(categorized).size();
    return new ImportSummary(inserted, transactions.size() - inserted);
  }
}
