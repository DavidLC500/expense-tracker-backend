package com.expensetracker.testdata;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para tests que necesitan un PostgreSQL real. El contenedor es un único campo {@code static}
 * en esta clase (patron "singleton container" de Testcontainers): todas las subclases lo comparten
 * y arranca una sola vez por JVM de test, en vez de uno por clase.
 */
@Tag("integration")
@Testcontainers
public abstract class PostgresIntegrationTest {

  @SuppressWarnings("resource")
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }
}
