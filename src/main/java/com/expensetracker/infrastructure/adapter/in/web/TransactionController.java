package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.ImportTransactionsFromCsvUseCase;
import com.expensetracker.application.port.in.ListTransactionMonthsUseCase;
import com.expensetracker.application.port.in.ListTransactionsUseCase;
import com.expensetracker.application.port.in.RecategorizeTransactionsUseCase;
import com.expensetracker.application.port.in.ResetTransactionCategoryUseCase;
import com.expensetracker.application.port.in.TransactionFilter;
import com.expensetracker.application.port.in.UpdateTransactionCategoryUseCase;
import com.expensetracker.application.port.out.BankProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoints de movimientos: importar CSV, listar con filtros (y los meses por los que filtrar),
 * corregir categoría o devolverla a las reglas, recategorizar.
 */
@Tag(
    name = "Transactions",
    description = "Movimientos bancarios: importarlos desde CSV, consultarlos y categorizarlos.")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final ListTransactionsUseCase listTransactionsUseCase;
  private final ListTransactionMonthsUseCase listTransactionMonthsUseCase;
  private final UpdateTransactionCategoryUseCase updateTransactionCategoryUseCase;
  private final ResetTransactionCategoryUseCase resetTransactionCategoryUseCase;
  private final RecategorizeTransactionsUseCase recategorizeTransactionsUseCase;
  private final ImportTransactionsFromCsvUseCase importTransactionsFromCsvUseCase;

  public TransactionController(
      ListTransactionsUseCase listTransactionsUseCase,
      ListTransactionMonthsUseCase listTransactionMonthsUseCase,
      UpdateTransactionCategoryUseCase updateTransactionCategoryUseCase,
      ResetTransactionCategoryUseCase resetTransactionCategoryUseCase,
      RecategorizeTransactionsUseCase recategorizeTransactionsUseCase,
      ImportTransactionsFromCsvUseCase importTransactionsFromCsvUseCase) {
    this.listTransactionsUseCase = listTransactionsUseCase;
    this.listTransactionMonthsUseCase = listTransactionMonthsUseCase;
    this.updateTransactionCategoryUseCase = updateTransactionCategoryUseCase;
    this.resetTransactionCategoryUseCase = resetTransactionCategoryUseCase;
    this.recategorizeTransactionsUseCase = recategorizeTransactionsUseCase;
    this.importTransactionsFromCsvUseCase = importTransactionsFromCsvUseCase;
  }

  @Operation(
      operationId = "importTransactionsCsv",
      summary = "Importar movimientos desde un CSV",
      description =
          """
          Formato: UTF-8 (con o sin BOM), cabecera `fecha,concepto,importe`, fecha \
          `AAAA-MM-DD` e importe con punto decimal (negativo = gasto). Se admiten columnas \
          extra y una coma final.

          Es idempotente: reimportar el mismo fichero no duplica nada, los movimientos que ya \
          existían cuentan como `duplicatesIgnored`. Cada movimiento nuevo se categoriza con \
          las reglas vigentes. Una fila inválida no detiene el resto: aparece en `errors` con \
          su número de fila y un código estable.""")
  @ApiResponse(responseCode = "200", description = "Resumen de la importación.")
  @ApiResponse(
      responseCode = "413",
      description = "El fichero supera el tamaño máximo (5 MB a través del frontend).")
  @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ImportResponse importCsv(
      @Parameter(description = "Extracto bancario en CSV.") @RequestParam("file")
          MultipartFile file)
      throws IOException {
    try (InputStream content = file.getInputStream()) {
      return ImportResponse.from(
          importTransactionsFromCsvUseCase.execute(content, BankProfile.generic()));
    }
  }

  @Operation(
      operationId = "listTransactions",
      summary = "Listar movimientos",
      description =
          "Del más reciente al más antiguo, paginados. Todos los filtros son opcionales y se"
              + " combinan entre sí.")
  @ApiResponse(responseCode = "200", description = "Página de movimientos.")
  @ApiResponse(
      responseCode = "400",
      description =
          "Filtro inválido: solo `year` o solo `month`, mes fuera de 1-12, `categoryId` junto"
              + " con `uncategorized`, o `pageSize` mayor que 100.")
  @GetMapping
  public TransactionPageResponse list(
      @Parameter(
              description = "Año del mes a filtrar; va siempre junto a `month`.",
              example = "2026")
          @RequestParam(required = false)
          Integer year,
      @Parameter(description = "Mes a filtrar (1-12); va siempre junto a `year`.", example = "1")
          @RequestParam(required = false)
          Integer month,
      @Parameter(description = "Solo los movimientos de esta categoría.")
          @RequestParam(required = false)
          UUID categoryId,
      @Parameter(
              description = "Solo los movimientos sin categoría. No se combina con `categoryId`.")
          @RequestParam(defaultValue = "false")
          boolean uncategorized,
      @Parameter(
              description = "Texto contenido en el concepto, sin distinguir mayúsculas ni tildes.",
              example = "mercadona")
          @RequestParam(required = false)
          String text,
      @Parameter(description = "Página, empezando en 0.") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Movimientos por página (máximo 100).")
          @RequestParam(defaultValue = "20")
          int pageSize) {
    TransactionFilter filter =
        new TransactionFilter(
            toYearMonth(year, month), categoryId, uncategorized, text, page, pageSize);
    return TransactionPageResponse.from(listTransactionsUseCase.execute(filter));
  }

  @Operation(
      operationId = "listTransactionMonths",
      summary = "Listar los meses con movimientos",
      description =
          "Del más reciente al más antiguo, con cuántos movimientos tiene cada uno. Solo aparecen"
              + " los meses con al menos uno: son los que tiene sentido ofrecer en el filtro por"
              + " mes de `listTransactions`.")
  @ApiResponse(responseCode = "200", description = "Meses con movimientos (vacío si no hay).")
  @GetMapping("/months")
  public List<TransactionMonthResponse> listMonths() {
    return listTransactionMonthsUseCase.execute().stream()
        .map(TransactionMonthResponse::from)
        .toList();
  }

  @Operation(
      operationId = "updateTransactionCategory",
      summary = "Corregir la categoría de un movimiento",
      description =
          "La corrección es manual: al reaplicar las reglas este movimiento conserva la"
              + " categoría elegida. `categoryId` a null lo deja sin categoría.")
  @ApiResponse(responseCode = "200", description = "Movimiento actualizado.")
  @ApiResponse(responseCode = "400", description = "El id no es un UUID o el cuerpo no es válido.")
  @ApiResponse(responseCode = "404", description = "No existe el movimiento o la categoría.")
  @PatchMapping("/{id}")
  public TransactionResponse updateCategory(
      @Parameter(description = "Id del movimiento.") @PathVariable UUID id,
      @RequestBody UpdateTransactionCategoryRequest request) {
    return TransactionResponse.from(
        updateTransactionCategoryUseCase.execute(id, request.categoryId()));
  }

  @Operation(
      operationId = "resetTransactionCategory",
      summary = "Deshacer la corrección manual de un movimiento",
      description =
          "La categoría vuelve a decidirla las reglas: se recalcula al momento con las vigentes"
              + " (o queda sin categoría si ninguna coincide) y las siguientes veces que se"
              + " reapliquen podrá cambiar. En un movimiento que no estaba corregido a mano, solo"
              + " recalcula.")
  @ApiResponse(responseCode = "200", description = "Movimiento con la categoría de las reglas.")
  @ApiResponse(responseCode = "400", description = "El id no es un UUID.")
  @ApiResponse(responseCode = "404", description = "No existe el movimiento.")
  @DeleteMapping("/{id}/manual-category")
  public TransactionResponse resetCategory(
      @Parameter(description = "Id del movimiento.") @PathVariable UUID id) {
    return TransactionResponse.from(resetTransactionCategoryUseCase.execute(id));
  }

  @Operation(
      operationId = "recategorizeTransactions",
      summary = "Reaplicar las reglas a los movimientos existentes",
      description =
          "Útil tras crear o borrar reglas. Los movimientos corregidos a mano no cambian. Devuelve"
              + " cuántos han cambiado de categoría.")
  @ApiResponse(responseCode = "200", description = "Número de movimientos que han cambiado.")
  @PostMapping("/recategorize")
  public RecategorizeResponse recategorize() {
    return new RecategorizeResponse(recategorizeTransactionsUseCase.execute());
  }

  private static YearMonth toYearMonth(Integer year, Integer month) {
    if (year == null && month == null) {
      return null;
    }
    if (year == null || month == null) {
      throw new IllegalArgumentException("year and month must be provided together");
    }
    return YearMonth.of(year, month);
  }
}
