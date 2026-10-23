package com.expensetracker.infrastructure.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expensetracker.application.port.in.GetMonthlyReportUseCase;
import com.expensetracker.domain.model.Money;
import com.expensetracker.domain.model.MonthlyReport;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private GetMonthlyReportUseCase getMonthlyReportUseCase;

  @Test
  void shouldReturnMonthlyReport() throws Exception {
    YearMonth month = YearMonth.of(2026, 3);
    MonthlyReport report =
        new MonthlyReport(
            month,
            Money.of(new BigDecimal("1500.00"), EUR),
            Money.of(new BigDecimal("400.00"), EUR),
            Money.of(new BigDecimal("1100.00"), EUR),
            List.of(),
            List.of(),
            Money.of(new BigDecimal("50.00"), EUR));
    given(getMonthlyReportUseCase.execute(month)).willReturn(report);

    mockMvc
        .perform(get("/api/reports/2026/3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.month").value(3))
        .andExpect(jsonPath("$.totalIncome").value(1500.00))
        .andExpect(jsonPath("$.totalExpense").value(400.00))
        .andExpect(jsonPath("$.previousMonthExpenseDifference").value(50.00));
  }

  @Test
  void shouldReturn400WhenMonthIsOutOfRange() throws Exception {
    mockMvc.perform(get("/api/reports/2026/0")).andExpect(status().isBadRequest());
  }
}
