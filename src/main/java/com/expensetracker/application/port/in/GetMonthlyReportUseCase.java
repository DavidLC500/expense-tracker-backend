package com.expensetracker.application.port.in;

import com.expensetracker.domain.model.MonthlyReport;
import java.time.YearMonth;

/**
 * Caso de uso: obtener el informe mensual de ingresos, gastos y comparación con el mes anterior.
 */
public interface GetMonthlyReportUseCase {

  MonthlyReport execute(YearMonth month);
}
