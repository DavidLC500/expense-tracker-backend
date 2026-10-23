package com.expensetracker.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.TransactionMonth;
import com.expensetracker.application.port.in.TransactionPage;
import com.expensetracker.domain.model.Transaction;
import com.expensetracker.testdata.PostgresIntegrationTest;
import com.expensetracker.testdata.Transactions;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaTransactionRepository.class, TransactionMapperImpl.class})
class JpaTransactionRepositoryTest extends PostgresIntegrationTest {

  @Autowired private JpaTransactionRepository transactionRepository;
  @Autowired private TestEntityManager entityManager;

  @Test
  void shouldSaveAndRetrieveTransactionById() {
    Transaction transaction = Transactions.transaction(LocalDate.of(2026, 1, 10), "100.00", null);

    transactionRepository.save(transaction);

    Optional<Transaction> found = transactionRepository.findById(transaction.id());

    assertThat(found).contains(transaction);
  }

  @Test
  void shouldInsertNewTransactionsAndSkipThoseWhoseImportHashAlreadyExists() {
    Transaction existing = Transactions.transaction(LocalDate.of(2026, 1, 5), "10.00", null);
    transactionRepository.save(existing);
    Transaction sameHash =
        new Transaction(
            UUID.randomUUID(),
            existing.date(),
            existing.concept(),
            existing.amount(),
            null,
            existing.importHash(),
            existing.createdAt());
    Transaction fresh = Transactions.transaction(LocalDate.of(2026, 1, 6), "20.00", null);

    List<Transaction> inserted =
        transactionRepository.insertIgnoringDuplicates(List.of(sameHash, fresh));

    assertThat(inserted).containsExactly(fresh);
    assertThat(transactionRepository.findAll()).hasSize(2);
    // Se compara por hash: PostgreSQL guarda created_at con microsegundos y Java tiene más
    // precisión.
    assertThat(transactionRepository.findById(fresh.id()))
        .map(Transaction::importHash)
        .contains(fresh.importHash());
    assertThat(transactionRepository.findById(sameHash.id())).isEmpty();
  }

  @Test
  void shouldInsertBatchesLargerThanOneRoundTripKeepingEveryRow() {
    List<Transaction> many =
        java.util.stream.IntStream.range(0, 1_200)
            .mapToObj(
                i ->
                    Transactions.transaction(
                        LocalDate.of(2026, 1, 1).plusDays(i % 28), i + ".00", null))
            .toList();

    List<Transaction> inserted = transactionRepository.insertIgnoringDuplicates(many);

    assertThat(inserted).hasSize(1_200);
    assertThat(transactionRepository.insertIgnoringDuplicates(many)).isEmpty();
  }

  @Test
  void shouldFindByMonthExcludingTransactionsFromOtherMonths() {
    Transaction inJanuary = Transactions.transaction(LocalDate.of(2026, 1, 15), "10.00", null);
    Transaction alsoInJanuary = Transactions.transaction(LocalDate.of(2026, 1, 1), "20.00", null);
    Transaction inFebruary = Transactions.transaction(LocalDate.of(2026, 2, 1), "30.00", null);

    transactionRepository.save(inJanuary);
    transactionRepository.save(alsoInJanuary);
    transactionRepository.save(inFebruary);

    List<Transaction> result = transactionRepository.findByMonth(YearMonth.of(2026, 1));

    assertThat(result).containsExactlyInAnyOrder(inJanuary, alsoInJanuary);
  }

  @Test
  void shouldCountTransactionsByMonthFromMostRecentAcrossYears() {
    transactionRepository.save(Transactions.transaction(LocalDate.of(2025, 12, 31), "1.00", null));
    transactionRepository.save(Transactions.transaction(LocalDate.of(2026, 1, 1), "2.00", null));
    transactionRepository.save(Transactions.transaction(LocalDate.of(2026, 1, 31), "3.00", null));
    transactionRepository.save(Transactions.transaction(LocalDate.of(2024, 11, 2), "4.00", null));
    transactionRepository.save(Transactions.transaction(LocalDate.of(2025, 2, 10), "5.00", null));

    assertThat(transactionRepository.countByMonth())
        .containsExactly(
            new TransactionMonth(YearMonth.of(2026, 1), 2),
            new TransactionMonth(YearMonth.of(2025, 12), 1),
            new TransactionMonth(YearMonth.of(2025, 2), 1),
            new TransactionMonth(YearMonth.of(2024, 11), 1));
  }

  @Test
  void shouldCountNoMonthsWhenThereAreNoTransactions() {
    assertThat(transactionRepository.countByMonth()).isEmpty();
  }

  @Test
  void shouldFindByFilterFilteringByMonth() {
    Transaction inJanuary = Transactions.transaction(LocalDate.of(2026, 1, 15), "10.00", null);
    Transaction inFebruary = Transactions.transaction(LocalDate.of(2026, 2, 1), "30.00", null);
    transactionRepository.save(inJanuary);
    transactionRepository.save(inFebruary);

    TransactionPage page =
        transactionRepository.findByFilter(
            new TransactionFilter(YearMonth.of(2026, 1), null, null, 0, 10));

    assertThat(page.items()).containsExactly(inJanuary);
    assertThat(page.totalElements()).isEqualTo(1);
  }

  @Test
  void shouldFindByFilterFilteringByCategory() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    Transaction withCategory =
        Transactions.transaction(LocalDate.of(2026, 1, 5), "15.00", categoryId);
    Transaction withoutCategory = Transactions.transaction(LocalDate.of(2026, 1, 6), "25.00", null);
    transactionRepository.save(withCategory);
    transactionRepository.save(withoutCategory);

    TransactionPage page =
        transactionRepository.findByFilter(new TransactionFilter(null, categoryId, null, 0, 10));

    assertThat(page.items()).containsExactly(withCategory);
    assertThat(page.totalElements()).isEqualTo(1);
  }

  @Test
  void shouldFindByFilterOnlyUncategorizedTransactions() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Ocio");
    Transaction withCategory =
        Transactions.transaction(LocalDate.of(2026, 1, 5), "15.00", categoryId);
    Transaction withoutCategory = Transactions.transaction(LocalDate.of(2026, 1, 6), "25.00", null);
    transactionRepository.save(withCategory);
    transactionRepository.save(withoutCategory);

    TransactionPage page =
        transactionRepository.findByFilter(new TransactionFilter(null, null, true, null, 0, 10));

    assertThat(page.items()).containsExactly(withoutCategory);
  }

  @Test
  void shouldPersistTheManualCategoryFlag() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Ocio");
    Transaction manual =
        Transactions.transaction(LocalDate.of(2026, 1, 5), "15.00", null)
            .withManualCategory(categoryId);

    transactionRepository.save(manual);

    assertThat(transactionRepository.findById(manual.id())).contains(manual);
  }

  @Test
  void shouldUpdateCategoriesInBatchLeavingOtherFieldsUntouched() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    Transaction first = Transactions.transaction(LocalDate.of(2026, 1, 5), "15.00", null);
    Transaction second = Transactions.transaction(LocalDate.of(2026, 1, 6), "25.00", categoryId);
    transactionRepository.save(first);
    transactionRepository.save(second);

    transactionRepository.updateCategories(
        List.of(first.withRuleCategory(categoryId), second.withRuleCategory(null)));
    // clear(): el UPDATE va por JDBC y la caché de JPA no lo ve. created_at se ignora porque
    // PostgreSQL lo guarda con microsegundos y Java tiene más precisión.
    entityManager.clear();

    assertThat(transactionRepository.findById(first.id()).orElseThrow())
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(first.withRuleCategory(categoryId));
    assertThat(transactionRepository.findById(second.id()).orElseThrow())
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(second.withRuleCategory(null));
  }

  @Test
  void shouldFindByFilterFilteringByText() {
    Transaction mercadona =
        Transactions.transaction(
            LocalDate.of(2026, 1, 5), "Compra Mercadona", new BigDecimal("15.00"), null);
    Transaction netflix =
        Transactions.transaction(
            LocalDate.of(2026, 1, 6), "Suscripcion Netflix", new BigDecimal("9.99"), null);
    transactionRepository.save(mercadona);
    transactionRepository.save(netflix);

    TransactionPage page =
        transactionRepository.findByFilter(new TransactionFilter(null, null, "mercadona", 0, 10));

    assertThat(page.items()).containsExactly(mercadona);
    assertThat(page.totalElements()).isEqualTo(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"cafeteria", "CAFETERÍA", "ñandu", "Ñandú"})
  void shouldFindByFilterFilteringByTextIgnoringAccents(String text) {
    Transaction cafeteria =
        Transactions.transaction(
            LocalDate.of(2026, 1, 5), "Cafetería El Ñandú", new BigDecimal("-3.50"), null);
    Transaction other =
        Transactions.transaction(
            LocalDate.of(2026, 1, 6), "Suscripcion Netflix", new BigDecimal("-9.99"), null);
    transactionRepository.save(cafeteria);
    transactionRepository.save(other);

    TransactionPage page =
        transactionRepository.findByFilter(new TransactionFilter(null, null, text, 0, 10));

    assertThat(page.items()).containsExactly(cafeteria);
  }

  @ParameterizedTest
  @ValueSource(strings = {"%", "_", "50%"})
  void shouldTreatLikeWildcardsInFilterTextAsLiteralCharacters(String text) {
    Transaction withPercent =
        Transactions.transaction(
            LocalDate.of(2026, 1, 5), "Descuento 50% TIENDA_X", new BigDecimal("-3.50"), null);
    Transaction other =
        Transactions.transaction(
            LocalDate.of(2026, 1, 6), "Suscripcion Netflix", new BigDecimal("-9.99"), null);
    transactionRepository.save(withPercent);
    transactionRepository.save(other);

    TransactionPage page =
        transactionRepository.findByFilter(new TransactionFilter(null, null, text, 0, 10));

    assertThat(page.items()).containsExactly(withPercent);
  }

  @Test
  void shouldPaginateFindByFilterResultsOrderedByDateDescending() {
    Transaction oldest = Transactions.transaction(LocalDate.of(2026, 1, 5), "10.00", null);
    Transaction middle = Transactions.transaction(LocalDate.of(2026, 1, 10), "20.00", null);
    Transaction newest = Transactions.transaction(LocalDate.of(2026, 1, 15), "30.00", null);
    transactionRepository.save(oldest);
    transactionRepository.save(middle);
    transactionRepository.save(newest);

    TransactionPage firstPage =
        transactionRepository.findByFilter(
            new TransactionFilter(YearMonth.of(2026, 1), null, null, 0, 2));
    TransactionPage secondPage =
        transactionRepository.findByFilter(
            new TransactionFilter(YearMonth.of(2026, 1), null, null, 1, 2));

    assertThat(firstPage.items()).containsExactly(newest, middle);
    assertThat(firstPage.totalElements()).isEqualTo(3);
    assertThat(secondPage.items()).containsExactly(oldest);
    assertThat(secondPage.totalElements()).isEqualTo(3);
  }
}
