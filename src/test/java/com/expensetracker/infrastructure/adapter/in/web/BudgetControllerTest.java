package com.expensetracker.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expensetracker.application.exception.BudgetNotFoundException;
import com.expensetracker.application.exception.CategoryNotFoundException;
import com.expensetracker.application.port.in.DeleteBudgetUseCase;
import com.expensetracker.application.port.in.GetBudgetStatusUseCase;
import com.expensetracker.application.port.in.SetBudgetsUseCase;
import com.expensetracker.domain.model.Budget;
import com.expensetracker.domain.model.BudgetStatus;
import com.expensetracker.domain.model.Money;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SetBudgetsUseCase setBudgetsUseCase;
  @MockitoBean private DeleteBudgetUseCase deleteBudgetUseCase;
  @MockitoBean private GetBudgetStatusUseCase getBudgetStatusUseCase;

  @Test
  void shouldSetBudgetsAndReturnThem() throws Exception {
    UUID categoryId = UUID.randomUUID();
    YearMonth month = YearMonth.of(2026, 3);
    Budget saved = Budget.of(categoryId, month, Money.of(new BigDecimal("300.00"), EUR));
    given(setBudgetsUseCase.execute(org.mockito.ArgumentMatchers.eq(month), any()))
        .willReturn(List.of(saved));

    mockMvc
        .perform(
            put("/api/budgets/2026/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"budgets":[{"categoryId":"%s","amount":300.00}]}
                    """
                        .formatted(categoryId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].amount").value(300.00))
        .andExpect(jsonPath("$[0].year").value(2026))
        .andExpect(jsonPath("$[0].month").value(3));
  }

  @Test
  void shouldReturn400WhenBudgetsListIsEmpty() throws Exception {
    mockMvc
        .perform(
            put("/api/budgets/2026/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"budgets\":[]}"))
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(strings = {"1e20", "10.555"})
  void shouldReturn400WhenAmountDoesNotFitColumnPrecision(String amount) throws Exception {
    mockMvc
        .perform(
            put("/api/budgets/2026/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"budgets":[{"categoryId":"%s","amount":%s}]}
                    """
                        .formatted(UUID.randomUUID(), amount)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenMonthIsOutOfRange() throws Exception {
    mockMvc
        .perform(
            put("/api/budgets/2026/13")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"budgets":[{"categoryId":"%s","amount":100.00}]}
                    """
                        .formatted(UUID.randomUUID())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn404WhenCategoryDoesNotExist() throws Exception {
    given(setBudgetsUseCase.execute(any(), any()))
        .willThrow(new CategoryNotFoundException(UUID.randomUUID()));

    mockMvc
        .perform(
            put("/api/budgets/2026/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"budgets":[{"categoryId":"%s","amount":100.00}]}
                    """
                        .formatted(UUID.randomUUID())))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteBudgetAndReturn204() throws Exception {
    UUID categoryId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/budgets/2026/3/{categoryId}", categoryId))
        .andExpect(status().isNoContent());

    then(deleteBudgetUseCase).should().execute(YearMonth.of(2026, 3), categoryId);
  }

  @Test
  void shouldReturn404WhenDeletingABudgetThatDoesNotExist() throws Exception {
    UUID categoryId = UUID.randomUUID();
    willThrow(new BudgetNotFoundException(categoryId, YearMonth.of(2026, 3)))
        .given(deleteBudgetUseCase)
        .execute(YearMonth.of(2026, 3), categoryId);

    mockMvc
        .perform(delete("/api/budgets/2026/3/{categoryId}", categoryId))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/api/budgets/2026/13/0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70",
        "/api/budgets/2026/3/abc"
      })
  void shouldReturn400WhenDeletingWithInvalidMonthOrCategoryId(String path) throws Exception {
    mockMvc.perform(delete(path)).andExpect(status().isBadRequest());

    then(deleteBudgetUseCase).shouldHaveNoInteractions();
  }

  @Test
  void shouldReturnBudgetStatus() throws Exception {
    UUID categoryId = UUID.randomUUID();
    YearMonth month = YearMonth.of(2026, 3);
    BudgetStatus status =
        new BudgetStatus(
            categoryId,
            month,
            Money.of(new BigDecimal("100.00"), EUR),
            Money.of(new BigDecimal("130.00"), EUR),
            Money.of(new BigDecimal("-30.00"), EUR),
            new BigDecimal("130.00"),
            true);
    given(getBudgetStatusUseCase.execute(month)).willReturn(List.of(status));

    mockMvc
        .perform(get("/api/budgets/2026/3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].exceeded").value(true))
        .andExpect(jsonPath("$[0].spent").value(130.00));
  }
}
