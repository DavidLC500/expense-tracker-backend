package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de la aplicación.
 *
 * <p>Vive en el paquete raíz (fuera de {@code domain}/{@code application}/{@code infrastructure})
 * para que el escaneo de componentes de Spring Boot cubra las tres capas sin necesidad de
 * {@code @ComponentScan} explícito.
 */
@SpringBootApplication
public final class ExpenseTrackerApplication {

  private ExpenseTrackerApplication() {}

  public static void main(String[] args) {
    SpringApplication.run(ExpenseTrackerApplication.class, args);
  }
}
