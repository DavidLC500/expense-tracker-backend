package com.expensetracker.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documento OpenAPI (Swagger UI en {@code /swagger-ui/index.html}). El frontend genera su cliente
 * HTTP a partir de él, así que los nombres de los tags son los de sus servicios.
 */
@Configuration
public class OpenApiConfig {

  private static final String PROBLEM_JSON = "application/problem+json";
  private static final String PROBLEM_SCHEMA = "ProblemDetail";

  private static final String DESCRIPTION =
      """
      API REST para importar movimientos bancarios desde CSV, categorizarlos con reglas, \
      controlar presupuestos mensuales y obtener un informe de cada mes.

      **Convenciones**
      - Importes en euros con 2 decimales. En los movimientos, negativo es gasto y positivo, \
      ingreso.
      - Fechas en formato ISO (`AAAA-MM-DD`). Un mes se indica con `year` y `month` (1-12).
      - Nombres de categoría, patrones de regla y búsquedas se comparan sin distinguir \
      mayúsculas, tildes ni espacios de más.
      - Los errores se devuelven como \
      [Problem Details (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457), con \
      `Content-Type: application/problem+json`.
      """;

  @Bean
  public OpenAPI expenseTrackerOpenApi() {
    return new OpenAPI()
        .info(new Info().title("Expense Tracker API").version("1.0.0").description(DESCRIPTION));
  }

  /**
   * Añade a todas las operaciones los errores que puede dar cualquiera (500 y 503) y pone el
   * formato ProblemDetail a todas las respuestas de error: los controllers solo declaran el código
   * y qué significa en cada endpoint. El esquema se registra aquí y no en el bean {@code OpenAPI}:
   * springdoc reconstruye los esquemas a partir de los DTO y descartaría uno declarado antes.
   */
  @Bean
  public OpenApiCustomizer problemDetailErrorResponses() {
    return openApi -> {
      if (openApi.getComponents() == null) {
        openApi.setComponents(new Components());
      }
      openApi.getComponents().addSchemas(PROBLEM_SCHEMA, problemDetailSchema());
      openApi
          .getPaths()
          .values()
          .forEach(
              path ->
                  path.readOperations()
                      .forEach(operation -> addProblemResponses(operation.getResponses())));
    };
  }

  private static void addProblemResponses(ApiResponses responses) {
    responses.putIfAbsent("500", new ApiResponse().description("Error inesperado del servidor."));
    responses.putIfAbsent(
        "503",
        new ApiResponse()
            .description(
                "La base de datos no está disponible en este momento; se puede reintentar."));
    responses.forEach(
        (code, response) -> {
          if (code.startsWith("4") || code.startsWith("5")) {
            response.setContent(problemContent());
          }
        });
  }

  private static Content problemContent() {
    return new Content()
        .addMediaType(
            PROBLEM_JSON,
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + PROBLEM_SCHEMA)));
  }

  private static Schema<?> problemDetailSchema() {
    return new ObjectSchema()
        .description("Error en formato RFC 9457 (Problem Details).")
        .addProperty("type", new StringSchema().example("about:blank"))
        .addProperty("title", new StringSchema().example("Not Found"))
        .addProperty("status", new IntegerSchema().example(404))
        .addProperty(
            "detail",
            new StringSchema()
                .description("Explicación técnica del error, en inglés.")
                .example("Category not found with id 0b7e1a52-6c3f-4d8e-9f1a-2b3c4d5e6f70"))
        .addProperty("instance", new StringSchema().example("/api/rules"))
        .addProperty(
            "fieldErrors",
            new MapSchema()
                .additionalProperties(new StringSchema())
                .description("Solo en errores de validación: el mensaje de cada campo inválido.")
                .example(Map.of("name", "must not be blank")));
  }
}
