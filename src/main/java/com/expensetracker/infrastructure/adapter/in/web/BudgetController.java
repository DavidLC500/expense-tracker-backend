package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.BudgetAmount;
import com.expensetracker.application.port.in.DeleteBudgetUseCase;
import com.expensetracker.application.port.in.GetBudgetStatusUseCase;
import com.expensetracker.application.port.in.SetBudgetsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de presupuestos: {@code PUT}/{@code GET /api/budgets/{year}/{month}} y {@code DELETE
 * /api/budgets/{year}/{month}/{categoryId}}.
 */
@Tag(name = "Budgets", description = "Presupuestos mensuales por categoría y cómo van.")
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

  private final SetBudgetsUseCase setBudgetsUseCase;
  private final DeleteBudgetUseCase deleteBudgetUseCase;
  private final GetBudgetStatusUseCase getBudgetStatusUseCase;

  public BudgetController(
      SetBudgetsUseCase setBudgetsUseCase,
      DeleteBudgetUseCase deleteBudgetUseCase,
      GetBudgetStatusUseCase getBudgetStatusUseCase) {
    this.setBudgetsUseCase = setBudgetsUseCase;
    this.deleteBudgetUseCase = deleteBudgetUseCase;
    this.getBudgetStatusUseCase = getBudgetStatusUseCase;
  }

  @Operation(
      operationId = "setBudgets",
      summary = "Definir los presupuestos de un mes",
      description =
          "Crea o actualiza el presupuesto de cada categoría enviada; las que no se envían no"
              + " cambian (para quitar uno, `deleteBudget`). Devuelve los presupuestos guardados.")
  @ApiResponse(responseCode = "200", description = "Presupuestos guardados.")
  @ApiResponse(
      responseCode = "400",
      description =
          "Mes fuera de 1-12, lista vacía, o importe negativo, con más de 2 decimales o de más"
              + " de 12 cifras enteras.")
  @ApiResponse(
      responseCode = "404",
      description = "No existe alguna de las categorías; en ese caso no se guarda ninguna.")
  @PutMapping("/{year}/{month}")
  public List<BudgetResponse> setBudgets(
      @Parameter(description = "Año.", example = "2026") @PathVariable int year,
      @Parameter(description = "Mes (1-12).", example = "1") @PathVariable int month,
      @Valid @RequestBody SetBudgetsRequest request) {
    List<BudgetAmount> amounts =
        request.budgets().stream().map(BudgetAmountRequest::toBudgetAmount).toList();
    return setBudgetsUseCase.execute(YearMonth.of(year, month), amounts).stream()
        .map(BudgetResponse::from)
        .toList();
  }

  @Operation(
      operationId = "deleteBudget",
      summary = "Quitar el presupuesto de una categoría en un mes",
      description =
          "La categoría deja de tener presupuesto ese mes; los de otros meses no cambian.")
  @ApiResponse(responseCode = "204", description = "Presupuesto quitado.")
  @ApiResponse(responseCode = "400", description = "Mes fuera de 1-12 o id que no es un UUID.")
  @ApiResponse(responseCode = "404", description = "Esa categoría no tiene presupuesto ese mes.")
  @DeleteMapping("/{year}/{month}/{categoryId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBudget(
      @Parameter(description = "Año.", example = "2026") @PathVariable int year,
      @Parameter(description = "Mes (1-12).", example = "1") @PathVariable int month,
      @Parameter(description = "Id de la categoría.") @PathVariable UUID categoryId) {
    deleteBudgetUseCase.execute(YearMonth.of(year, month), categoryId);
  }

  @Operation(
      operationId = "getBudgetStatus",
      summary = "Consultar cómo van los presupuestos de un mes",
      description =
          "Por cada categoría con presupuesto: lo presupuestado, lo gastado, lo que queda, el"
              + " porcentaje usado y si se ha excedido. Solo cuentan los gastos (importes"
              + " negativos) de esa categoría y mes.")
  @ApiResponse(
      responseCode = "200",
      description = "Estado de cada presupuesto del mes; vacío si no hay ninguno.")
  @ApiResponse(responseCode = "400", description = "Mes fuera de 1-12.")
  @GetMapping("/{year}/{month}")
  public List<BudgetStatusResponse> getStatus(
      @Parameter(description = "Año.", example = "2026") @PathVariable int year,
      @Parameter(description = "Mes (1-12).", example = "1") @PathVariable int month) {
    return getBudgetStatusUseCase.execute(YearMonth.of(year, month)).stream()
        .map(BudgetStatusResponse::from)
        .toList();
  }
}
