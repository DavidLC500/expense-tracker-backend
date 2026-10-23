package com.expensetracker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.expensetracker.testdata.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Comprueba que el contexto de Spring arranca (con un PostgreSQL real de Testcontainers detrás,
 * desde la Fase 3) y que el healthcheck de Actuator responde.
 *
 * <p>Se usa {@code @SpringBootTest} con puerto aleatorio (en vez de {@code @WebMvcTest}) porque
 * {@code /actuator/health} lo expone la autoconfiguración de Actuator, no un {@code @Controller} de
 * la aplicación; un slice test de MVC no lo cargaría.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
class HealthEndpointTest extends PostgresIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void shouldReturnUpStatusFromHealthEndpoint() {
    ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"status\":\"UP\"");
  }
}
