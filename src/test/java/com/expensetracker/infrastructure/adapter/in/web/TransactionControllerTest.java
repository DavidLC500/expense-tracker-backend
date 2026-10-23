package com.expensetracker.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expensetracker.application.exception.TransactionNotFoundException;
import com.expensetracker.application.port.in.CsvImportSummary;
import com.expensetracker.application.port.in.ImportTransactionsFromCsvUseCase;
import com.expensetracker.application.port.in.ListTransactionMonthsUseCase;
import com.expensetracker.application.port.in.ListTransactionsUseCase;
import com.expensetracker.application.port.in.RecategorizeTransactionsUseCase;
import com.expensetracker.application.port.in.ResetTransactionCategoryUseCase;
import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.TransactionMonth;
import com.expensetracker.application.port.in.TransactionPage;
import com.expensetracker.application.port.in.UpdateTransactionCategoryUseCase;
import com.expensetracker.application.port.out.RowError;
import com.expensetracker.application.port.out.RowErrorCode;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ListTransactionsUseCase listTransactionsUseCase;
  @MockitoBean private ListTransactionMonthsUseCase listTransactionMonthsUseCase;
  @MockitoBean private UpdateTransactionCategoryUseCase updateTransactionCategoryUseCase;
  @MockitoBean private ResetTransactionCategoryUseCase resetTransactionCategoryUseCase;
  @MockitoBean private ImportTransactionsFromCsvUseCase importTransactionsFromCsvUseCase;
  @MockitoBean private RecategorizeTransactionsUseCase recategorizeTransactionsUseCase;

  private static Transaction sampleTransaction(UUID categoryId) {
    return new Transaction(
        UUID.randomUUID(),
        LocalDate.of(2026, 3, 15),
        "MERCADONA MADRID",
        Money.of(new BigDecimal("-42.50"), EUR),
        categoryId,
        "hash-123",
        Instant.parse("2026-03-15T10:00:00Z"));
  }

  @Test
  void shouldListTransactionsWithNoFilters() throws Exception {
    Transaction transaction = sampleTransaction(null);
    given(listTransactionsUseCase.execute(new TransactionFilter(null, null, null, 0, 20)))
        .willReturn(new TransactionPage(List.of(transaction), 0, 20, 1));

    mockMvc
        .perform(get("/api/transactions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", org.hamcrest.Matchers.hasSize(1)))
        .andExpect(jsonPath("$.items[0].concept").value("MERCADONA MADRID"))
        .andExpect(jsonPath("$.items[0].amount").value(-42.50))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void shouldListTransactionsFilteredByYearAndMonth() throws Exception {
    given(
            listTransactionsUseCase.execute(
                new TransactionFilter(YearMonth.of(2026, 3), null, null, 0, 20)))
        .willReturn(new TransactionPage(List.of(), 0, 20, 0));

    mockMvc
        .perform(get("/api/transactions").param("year", "2026").param("month", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void shouldListOnlyUncategorizedTransactions() throws Exception {
    given(listTransactionsUseCase.execute(new TransactionFilter(null, null, true, null, 0, 20)))
        .willReturn(new TransactionPage(List.of(sampleTransaction(null)), 0, 20, 1));

    mockMvc
        .perform(get("/api/transactions").param("uncategorized", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void shouldReturn400WhenFilteringByCategoryAndUncategorizedAtOnce() throws Exception {
    mockMvc
        .perform(
            get("/api/transactions")
                .param("categoryId", UUID.randomUUID().toString())
                .param("uncategorized", "true"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenOnlyYearIsProvidedWithoutMonth() throws Exception {
    mockMvc
        .perform(get("/api/transactions").param("year", "2026"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldListMonthsWithTransactions() throws Exception {
    given(listTransactionMonthsUseCase.execute())
        .willReturn(
            List.of(
                new TransactionMonth(YearMonth.of(2026, 1), 2),
                new TransactionMonth(YearMonth.of(2025, 12), 7)));

    mockMvc
        .perform(get("/api/transactions/months"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
        .andExpect(jsonPath("$[0].year").value(2026))
        .andExpect(jsonPath("$[0].month").value(1))
        .andExpect(jsonPath("$[0].count").value(2))
        .andExpect(jsonPath("$[1].year").value(2025))
        .andExpect(jsonPath("$[1].month").value(12))
        .andExpect(jsonPath("$[1].count").value(7));
  }

  @Test
  void shouldUpdateTransactionCategory() throws Exception {
    UUID transactionId = UUID.randomUUID();
    UUID newCategoryId = UUID.randomUUID();
    Transaction updated = sampleTransaction(newCategoryId);
    given(updateTransactionCategoryUseCase.execute(eq(transactionId), eq(newCategoryId)))
        .willReturn(updated);

    mockMvc
        .perform(
            patch("/api/transactions/{id}", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":\"%s\"}".formatted(newCategoryId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categoryId").value(newCategoryId.toString()));
  }

  @Test
  void shouldExposeWhetherTheCategoryWasCorrectedManually() throws Exception {
    UUID transactionId = UUID.randomUUID();
    UUID newCategoryId = UUID.randomUUID();
    given(updateTransactionCategoryUseCase.execute(eq(transactionId), eq(newCategoryId)))
        .willReturn(sampleTransaction(null).withManualCategory(newCategoryId));

    mockMvc
        .perform(
            patch("/api/transactions/{id}", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":\"%s\"}".formatted(newCategoryId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.manuallyCategorized").value(true));
  }

  @Test
  void shouldHandTheCategoryBackToTheRules() throws Exception {
    UUID transactionId = UUID.randomUUID();
    UUID ruleCategory = UUID.randomUUID();
    given(resetTransactionCategoryUseCase.execute(transactionId))
        .willReturn(sampleTransaction(null).withRuleCategory(ruleCategory));

    mockMvc
        .perform(delete("/api/transactions/{id}/manual-category", transactionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categoryId").value(ruleCategory.toString()))
        .andExpect(jsonPath("$.manuallyCategorized").value(false));
  }

  @Test
  void shouldReturn404WhenResettingUnknownTransaction() throws Exception {
    UUID transactionId = UUID.randomUUID();
    given(resetTransactionCategoryUseCase.execute(transactionId))
        .willThrow(new TransactionNotFoundException(transactionId));

    mockMvc
        .perform(delete("/api/transactions/{id}/manual-category", transactionId))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenUpdatingUnknownTransaction() throws Exception {
    UUID transactionId = UUID.randomUUID();
    given(updateTransactionCategoryUseCase.execute(any(), any()))
        .willThrow(new TransactionNotFoundException(transactionId));

    mockMvc
        .perform(
            patch("/api/transactions/{id}", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":null}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnChangedCountOnRecategorize() throws Exception {
    given(recategorizeTransactionsUseCase.execute()).willReturn(3);

    mockMvc
        .perform(post("/api/transactions/recategorize"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.changed").value(3));
  }

  @Test
  void shouldImportCsvAndReturnSummaryWithRowErrors() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "movimientos.csv", "text/csv", "fecha,concepto,importe".getBytes());
    CsvImportSummary summary =
        new CsvImportSummary(2, 1, List.of(new RowError(4, RowErrorCode.INVALID_DATE, "bad row")));
    given(importTransactionsFromCsvUseCase.execute(any(), any())).willReturn(summary);

    mockMvc
        .perform(multipart("/api/transactions/import").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.imported").value(2))
        .andExpect(jsonPath("$.duplicatesIgnored").value(1))
        .andExpect(jsonPath("$.errors[0].row").value(4))
        .andExpect(jsonPath("$.errors[0].code").value("INVALID_DATE"))
        .andExpect(jsonPath("$.errors[0].reason").value("bad row"));
  }
}
