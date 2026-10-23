package com.expensetracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.expensetracker.infrastructure.adapter.out.persistence.CategoryJpaRepository;
import com.expensetracker.infrastructure.adapter.out.persistence.RuleJpaRepository;
import com.expensetracker.infrastructure.adapter.out.persistence.TransactionJpaRepository;
import com.expensetracker.testdata.PostgresIntegrationTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Test de extremo a extremo de la Fase 5: sube el CSV real de {@code examples/} a la API completa
 * (contra PostgreSQL real de Testcontainers), crea una regla de categorización antes de importar, y
 * reimporta el mismo fichero para comprobar la idempotencia de verdad (no simulada con fakes).
 *
 * <p>A diferencia de {@code @DataJpaTest}, {@code @SpringBootTest} no envuelve el test en una
 * transacción que se deshace sola (la petición HTTP real corre en otro hilo/transacción), así que
 * hay que limpiar a mano en {@link #cleanUpDatabase()} para no contaminar con datos persistentes el
 * PostgreSQL compartido (patrón "singleton container") que usan el resto de tests de integración.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CsvImportEndToEndTest extends PostgresIntegrationTest {

  private static final int EXAMPLE_CSV_ROW_COUNT = 7;

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private TransactionJpaRepository transactionJpaRepository;
  @Autowired private RuleJpaRepository ruleJpaRepository;
  @Autowired private CategoryJpaRepository categoryJpaRepository;

  @AfterEach
  void cleanUpDatabase() {
    transactionJpaRepository.deleteAll();
    ruleJpaRepository.deleteAll();
    categoryJpaRepository.deleteAll();
  }

  @Test
  void shouldImportExampleCsvCategorizeAndBeIdempotentOnReimport() throws Exception {
    String categoryId = createCategory("Alimentación");
    createRule("MERCADONA", categoryId, 1);

    ResponseEntity<String> firstImport = importExampleCsv();

    assertThat(firstImport.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(firstImport.getBody()).contains("\"imported\":" + EXAMPLE_CSV_ROW_COUNT);
    assertThat(firstImport.getBody()).contains("\"duplicatesIgnored\":0");

    ResponseEntity<String> transactions =
        restTemplate.getForEntity("/api/transactions?year=2026&month=1&pageSize=20", String.class);
    assertThat(transactions.getBody()).contains("\"totalElements\":" + EXAMPLE_CSV_ROW_COUNT);
    assertThat(transactions.getBody()).contains("\"categoryId\":\"" + categoryId + "\"");

    ResponseEntity<String> secondImport = importExampleCsv();

    assertThat(secondImport.getBody()).contains("\"imported\":0");
    assertThat(secondImport.getBody()).contains("\"duplicatesIgnored\":" + EXAMPLE_CSV_ROW_COUNT);
  }

  private String createCategory(String name) {
    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "/api/categories", jsonEntity("{\"name\":\"%s\"}".formatted(name)), String.class);
    return extractField(response.getBody(), "id");
  }

  private void createRule(String pattern, String categoryId, int priority) {
    restTemplate.postForEntity(
        "/api/rules",
        jsonEntity(
            "{\"pattern\":\"%s\",\"categoryId\":\"%s\",\"priority\":%d}"
                .formatted(pattern, categoryId, priority)),
        String.class);
  }

  private ResponseEntity<String> importExampleCsv() throws Exception {
    byte[] csvBytes = Files.readAllBytes(Path.of("examples/movimientos-ejemplo.csv"));
    ByteArrayResource fileResource =
        new ByteArrayResource(csvBytes) {
          @Override
          public String getFilename() {
            return "movimientos-ejemplo.csv";
          }
        };

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", fileResource);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    return restTemplate.postForEntity("/api/transactions/import", request, String.class);
  }

  private static HttpEntity<String> jsonEntity(String json) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(json, headers);
  }

  private static String extractField(String json, String field) {
    String marker = "\"" + field + "\":\"";
    int start = json.indexOf(marker) + marker.length();
    int end = json.indexOf('"', start);
    return json.substring(start, end);
  }
}
