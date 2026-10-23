package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.domain.model.MonthlyReport;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/** Respuesta JSON de {@code GET /api/reports/{year}/{month}}. */
@Schema(description = "Informe de un mes.")
public record MonthlyReportResponse(
    @Schema(description = "Año.", example = "2026") int year,
    @Schema(description = "Mes (1-12).", example = "1") int month,
    @Schema(description = "Suma de los importes positivos.", example = "1800.00")
        BigDecimal totalIncome,
    @Schema(description = "Suma de los gastos, en positivo.", example = "828.89")
        BigDecimal totalExpense,
    @Schema(description = "Ingresos menos gastos.", example = "971.11") BigDecimal balance,
    @Schema(description = "Divisa (ISO 4217).", example = "EUR") String currency,
    @Schema(description = "Gasto por categoría, de mayor a menor.")
        List<CategoryExpenseResponse> expensesByCategory,
    @Schema(description = "Los 5 mayores gastos del mes, del mayor al menor.")
        List<TransactionResponse> top5Expenses,
    @Schema(
            description = "Gasto de este mes menos el del anterior: positivo si se ha gastado más.",
            example = "728.89")
        BigDecimal previousMonthExpenseDifference) {

  public MonthlyReportResponse {
    expensesByCategory = List.copyOf(expensesByCategory);
    top5Expenses = List.copyOf(top5Expenses);
  }

  static MonthlyReportResponse from(MonthlyReport report) {
    return new MonthlyReportResponse(
        report.month().getYear(),
        report.month().getMonthValue(),
        report.totalIncome().amount(),
        report.totalExpense().amount(),
        report.balance().amount(),
        report.totalIncome().currency().getCurrencyCode(),
        report.expensesByCategory().stream().map(CategoryExpenseResponse::from).toList(),
        report.top5Expenses().stream().map(TransactionResponse::from).toList(),
        report.previousMonthExpenseDifference().amount());
  }
}
