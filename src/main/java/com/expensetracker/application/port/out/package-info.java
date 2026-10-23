/**
 * Puertos de salida: interfaces que la capa de aplicación necesita e implementa {@code
 * infrastructure} (repositorios, parser de CSV, reloj).
 *
 * <p>Java puro: este paquete no puede depender de Spring, JPA ni de {@code infrastructure} (regla
 * verificada por ArchUnit).
 */
package com.expensetracker.application.port.out;
