package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.GetMonthlyReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint del informe mensual: {@code GET /api/reports/{year}/{month}}. */
@Tag(name = "Reports", description = "Informe de ingresos y gastos de un mes.")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

  private final GetMonthlyReportUseCase getMonthlyReportUseCase;

  public ReportController(GetMonthlyReportUseCase getMonthlyReportUseCase) {
    this.getMonthlyReportUseCase = getMonthlyReportUseCase;
  }

  @Operation(
      operationId = "getMonthlyReport",
      summary = "Obtener el informe de un mes",
      description =
          "Ingresos, gastos y balance; gasto por categoría (los movimientos sin categoría van"
              + " con `categoryId` null); los 5 mayores gastos, y la diferencia de gasto con el"
              + " mes anterior. Un mes sin movimientos devuelve todo a cero.")
  @ApiResponse(responseCode = "200", description = "Informe del mes.")
  @ApiResponse(responseCode = "400", description = "Mes fuera de 1-12.")
  @GetMapping("/{year}/{month}")
  public MonthlyReportResponse getReport(
      @Parameter(description = "Año.", example = "2026") @PathVariable int year,
      @Parameter(description = "Mes (1-12).", example = "1") @PathVariable int month) {
    return MonthlyReportResponse.from(getMonthlyReportUseCase.execute(YearMonth.of(year, month)));
  }
}
