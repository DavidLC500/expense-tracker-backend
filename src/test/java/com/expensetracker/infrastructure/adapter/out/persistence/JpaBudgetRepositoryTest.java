package com.expensetracker.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.Money;
import com.expensetracker.testdata.PostgresIntegrationTest;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaBudgetRepository.class, BudgetMapperImpl.class})
class JpaBudgetRepositoryTest extends PostgresIntegrationTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @Autowired private JpaBudgetRepository budgetRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  void shouldSaveNewBudgetAndRetrieveItByMonth() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    YearMonth month = YearMonth.of(2026, 1);
    Budget budget = Budget.of(categoryId, month, Money.of(new BigDecimal("300.00"), EUR));

    budgetRepository.save(budget);

    List<Budget> found = budgetRepository.findByMonth(month);

    assertThat(found).hasSize(1);
    assertThat(found.get(0).categoryId()).isEqualTo(categoryId);
    assertThat(found.get(0).month()).isEqualTo(month);
    assertThat(found.get(0).amount()).isEqualTo(Money.of(new BigDecimal("300.00"), EUR));
  }

  @Test
  void shouldUpsertByCategoryAndMonthKeepingASingleRowWithTheLatestAmount() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    YearMonth month = YearMonth.of(2026, 2);

    budgetRepository.save(Budget.of(categoryId, month, Money.of(new BigDecimal("300.00"), EUR)));
    budgetRepository.save(Budget.of(categoryId, month, Money.of(new BigDecimal("450.00"), EUR)));

    List<Budget> found = budgetRepository.findByMonth(month);

    assertThat(found).hasSize(1);
    assertThat(found.get(0).amount()).isEqualTo(Money.of(new BigDecimal("450.00"), EUR));
  }

  @Test
  void shouldNotReturnBudgetsFromOtherMonthsOnFindByMonth() {
    UUID categoryId = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    YearMonth january = YearMonth.of(2026, 1);
    YearMonth february = YearMonth.of(2026, 2);

    budgetRepository.save(Budget.of(categoryId, january, Money.of(new BigDecimal("100.00"), EUR)));
    budgetRepository.save(Budget.of(categoryId, february, Money.of(new BigDecimal("200.00"), EUR)));

    List<Budget> found = budgetRepository.findByMonth(january);

    assertThat(found).hasSize(1);
    assertThat(found.get(0).month()).isEqualTo(january);
    assertThat(found.get(0).amount()).isEqualTo(Money.of(new BigDecimal("100.00"), EUR));
  }

  @Test
  void shouldDeleteOnlyTheBudgetOfThatCategoryAndMonth() {
    UUID food = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    UUID leisure = CategoryFixtures.persistCategory(entityManager, "Ocio");
    YearMonth march = YearMonth.of(2026, 3);
    YearMonth april = YearMonth.of(2026, 4);
    budgetRepository.save(Budget.of(food, march, Money.of(new BigDecimal("300.00"), EUR)));
    budgetRepository.save(Budget.of(leisure, march, Money.of(new BigDecimal("50.00"), EUR)));
    budgetRepository.save(Budget.of(food, april, Money.of(new BigDecimal("310.00"), EUR)));

    boolean deleted = budgetRepository.delete(food, march);

    assertThat(deleted).isTrue();
    assertThat(budgetRepository.findByMonth(march))
        .extracting(Budget::categoryId)
        .containsExactly(leisure);
    assertThat(budgetRepository.findByMonth(april))
        .extracting(Budget::categoryId)
        .containsExactly(food);
  }

  @Test
  void shouldReturnFalseWhenDeletingABudgetThatDoesNotExist() {
    UUID food = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    YearMonth march = YearMonth.of(2026, 3);
    budgetRepository.save(Budget.of(food, march, Money.of(new BigDecimal("300.00"), EUR)));

    assertThat(budgetRepository.delete(food, YearMonth.of(2026, 4))).isFalse();
    assertThat(budgetRepository.delete(UUID.randomUUID(), march)).isFalse();
    assertThat(budgetRepository.findByMonth(march)).hasSize(1);
  }

  @Test
  void shouldSaveAgainABudgetThatWasDeleted() {
    UUID food = CategoryFixtures.persistCategory(entityManager, "Alimentación");
    YearMonth march = YearMonth.of(2026, 3);
    budgetRepository.save(Budget.of(food, march, Money.of(new BigDecimal("300.00"), EUR)));
    budgetRepository.delete(food, march);

    budgetRepository.save(Budget.of(food, march, Money.of(new BigDecimal("120.00"), EUR)));

    assertThat(budgetRepository.findByMonth(march))
        .singleElement()
        .extracting(Budget::amount)
        .isEqualTo(Money.of(new BigDecimal("120.00"), EUR));
  }
}
