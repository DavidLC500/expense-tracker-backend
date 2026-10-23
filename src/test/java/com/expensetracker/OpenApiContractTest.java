package com.expensetracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.expensetracker.testdata.PostgresIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Contrato del documento OpenAPI. El cliente del frontend se genera desde él (si las respuestas se
 * publican con el media type comodín en vez de {@code application/json}, el cliente generado las
 * trata como Blob) y es la documentación de la API en Swagger UI: estos tests impiden que una
 * operación nueva se publique sin describir o sin sus errores.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OpenApiContractTest extends PostgresIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void shouldPublishJsonResponsesInOpenApiDocument() {
    ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"application/json\"").doesNotContain("\"*/*\"");
  }

  @Test
  void shouldDescribeTheApiAndEachGroupOfEndpoints() throws Exception {
    JsonNode document = apiDocs();

    assertThat(document.at("/info/title").asText()).isEqualTo("Expense Tracker API");
    assertThat(document.at("/info/description").asText()).isNotBlank();
    List<String> tagNames = new ArrayList<>();
    for (JsonNode tag : document.get("tags")) {
      tagNames.add(tag.get("name").asText());
      assertThat(tag.path("description").asText()).as(tag.toString()).isNotBlank();
    }
    assertThat(tagNames)
        .containsExactlyInAnyOrder("Transactions", "Categories", "Rules", "Budgets", "Reports");
  }

  @Test
  void shouldDescribeEveryOperationAndItsErrorsAsProblemDetail() throws Exception {
    JsonNode document = apiDocs();
    List<String> problems = new ArrayList<>();

    // Las respuestas de error apuntan a este esquema: si no se publica, el generador del cliente
    // del frontend rechaza el documento entero.
    JsonNode problemDetail = document.at("/components/schemas/ProblemDetail/properties");
    assertThat(problemDetail.has("status") && problemDetail.has("detail"))
        .as("components.schemas.ProblemDetail publicado")
        .isTrue();

    for (Map.Entry<String, JsonNode> path : document.get("paths").properties()) {
      for (Map.Entry<String, JsonNode> operation : path.getValue().properties()) {
        String name = operation.getKey().toUpperCase() + " " + path.getKey();
        JsonNode op = operation.getValue();
        if (op.path("summary").asText().isBlank() || op.path("description").asText().isBlank()) {
          problems.add(name + ": sin summary o description");
        }
        JsonNode responses = op.get("responses");
        if (!responses.has("500") || !responses.has("503")) {
          problems.add(name + ": faltan los errores 500 y 503");
        }
        for (Map.Entry<String, JsonNode> response : responses.properties()) {
          boolean isError = response.getKey().startsWith("4") || response.getKey().startsWith("5");
          String schema =
              response.getValue().at("/content/application~1problem+json/schema/$ref").asText();
          if (isError && !schema.endsWith("/ProblemDetail")) {
            problems.add(name + " " + response.getKey() + ": sin formato ProblemDetail");
          }
        }
      }
    }

    assertThat(problems).isEmpty();
  }

  @Test
  void shouldDocumentTheErrorsSpecificToEachEndpoint() throws Exception {
    JsonNode paths = apiDocs().get("paths");

    assertThat(paths.at("/~1api~1transactions~1{id}/patch/responses").has("404")).isTrue();
    assertThat(paths.at("/~1api~1transactions~1import/post/responses").has("413")).isTrue();
    assertThat(paths.at("/~1api~1categories/post/responses").has("409")).isTrue();
    assertThat(paths.at("/~1api~1rules/post/responses").has("404")).isTrue();
    assertThat(
            paths.at("/~1api~1budgets~1{year}~1{month}~1{categoryId}/delete/responses").has("404"))
        .isTrue();
  }

  private JsonNode apiDocs() throws Exception {
    return JSON.readTree(restTemplate.getForObject("/v3/api-docs", String.class));
  }
}
