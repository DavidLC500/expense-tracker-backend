package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.CreateRuleUseCase;
import com.expensetracker.application.port.in.DeleteRuleUseCase;
import com.expensetracker.application.port.in.ListRulesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de reglas de categorización: {@code GET}/{@code POST /api/rules}, {@code DELETE
 * /api/rules/{id}}.
 */
@Tag(
    name = "Rules",
    description =
        "Reglas de categorización automática: si el concepto de un movimiento contiene el patrón,"
            + " recibe la categoría de la regla.")
@RestController
@RequestMapping("/api/rules")
public class RuleController {

  private final CreateRuleUseCase createRuleUseCase;
  private final ListRulesUseCase listRulesUseCase;
  private final DeleteRuleUseCase deleteRuleUseCase;

  public RuleController(
      CreateRuleUseCase createRuleUseCase,
      ListRulesUseCase listRulesUseCase,
      DeleteRuleUseCase deleteRuleUseCase) {
    this.createRuleUseCase = createRuleUseCase;
    this.listRulesUseCase = listRulesUseCase;
    this.deleteRuleUseCase = deleteRuleUseCase;
  }

  @Operation(
      operationId = "listRules",
      summary = "Listar reglas",
      description = "Ordenadas por prioridad (la de número más bajo primero), patrón e id.")
  @ApiResponse(responseCode = "200", description = "Todas las reglas.")
  @GetMapping
  public List<RuleResponse> list() {
    return listRulesUseCase.execute().stream().map(RuleResponse::from).toList();
  }

  @Operation(
      operationId = "createRule",
      summary = "Crear una regla",
      description =
          "El patrón se guarda normalizado (sin tildes y en mayúsculas). Si varias reglas"
              + " coinciden con un concepto, gana la de prioridad más baja. Se aplica a las"
              + " importaciones siguientes; para los movimientos ya importados, usa"
              + " `POST /api/transactions/recategorize`.")
  @ApiResponse(responseCode = "201", description = "Regla creada.")
  @ApiResponse(
      responseCode = "400",
      description = "Patrón vacío o de más de 255 caracteres, o falta la prioridad.")
  @ApiResponse(responseCode = "404", description = "No existe la categoría indicada.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RuleResponse create(@Valid @RequestBody CreateRuleRequest request) {
    return RuleResponse.from(
        createRuleUseCase.execute(request.pattern(), request.categoryId(), request.priority()));
  }

  @Operation(
      operationId = "deleteRule",
      summary = "Borrar una regla",
      description =
          "Los movimientos que ya categorizó conservan su categoría hasta que se reapliquen las"
              + " reglas.")
  @ApiResponse(responseCode = "204", description = "Regla borrada.")
  @ApiResponse(responseCode = "400", description = "El id no es un UUID.")
  @ApiResponse(responseCode = "404", description = "No existe la regla.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@Parameter(description = "Id de la regla.") @PathVariable UUID id) {
    deleteRuleUseCase.execute(id);
  }
}
