package com.expensetracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.application.exception.TransactionNotFoundException;
import com.expensetracker.domain.model.Category;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.testdata.InMemoryCategoryRepository;
import com.expensetracker.testdata.InMemoryTransactionRepository;
import com.expensetracker.testdata.Transactions;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateTransactionCategoryServiceTest {

  private InMemoryTransactionRepository transactionRepository;
  private InMemoryCategoryRepository categoryRepository;
  private UpdateTransactionCategoryService service;

  @BeforeEach
  void setUp() {
    transactionRepository = new InMemoryTransactionRepository();
    categoryRepository = new InMemoryCategoryRepository();
    service = new UpdateTransactionCategoryService(transactionRepository, categoryRepository);
  }

  @Test
  void shouldUpdateCategoryOfExistingTransaction() {
    Transaction stored =
        transactionRepository.save(Transactions.expense(LocalDate.of(2026, 3, 15), "42.50", null));
    UUID newCategoryId = categoryRepository.save(Category.of("Ocio")).id();

    Transaction updated = service.execute(stored.id(), newCategoryId);

    assertThat(updated.categoryId()).isEqualTo(newCategoryId);
    assertThat(transactionRepository.findById(stored.id()).orElseThrow().categoryId())
        .isEqualTo(newCategoryId);
  }

  @Test
  void shouldMarkTheCorrectionAsManualSoRulesDoNotOverrideIt() {
    Transaction stored =
        transactionRepository.save(Transactions.expense(LocalDate.of(2026, 3, 15), "42.50", null));
    UUID newCategoryId = categoryRepository.save(Category.of("Ocio")).id();

    service.execute(stored.id(), newCategoryId);

    assertThat(transactionRepository.findById(stored.id()).orElseThrow().manuallyCategorized())
        .isTrue();
  }

  @Test
  void shouldThrowTransactionNotFoundExceptionWhenIdDoesNotExist() {
    UUID unknownId = UUID.randomUUID();

    assertThatThrownBy(() -> service.execute(unknownId, UUID.randomUUID()))
        .isInstanceOf(TransactionNotFoundException.class);
  }

  @Test
  void shouldThrowCategoryNotFoundExceptionWithoutChangingTransactionWhenCategoryDoesNotExist() {
    UUID originalCategoryId = categoryRepository.save(Category.of("Ocio")).id();
    Transaction stored =
        transactionRepository.save(
            Transactions.expense(LocalDate.of(2026, 3, 15), "42.50", originalCategoryId));

    assertThatThrownBy(() -> service.execute(stored.id(), UUID.randomUUID()))
        .isInstanceOf(CategoryNotFoundException.class);
    assertThat(transactionRepository.findById(stored.id()).orElseThrow().categoryId())
        .isEqualTo(originalCategoryId);
  }

  @Test
  void shouldClearCategoryWhenNewCategoryIdIsNull() {
    UUID categoryId = categoryRepository.save(Category.of("Ocio")).id();
    Transaction stored =
        transactionRepository.save(
            Transactions.expense(LocalDate.of(2026, 3, 15), "42.50", categoryId));

    Transaction updated = service.execute(stored.id(), null);

    assertThat(updated.categoryId()).isNull();
  }
}
