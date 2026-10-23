/**
 * Puertos de entrada: interfaces que declaran los casos de uso de la aplicación (p. ej. importar
 * movimientos, recategorizar, consultar presupuestos).
 *
 * <p>Java puro: este paquete no puede depender de Spring, JPA ni de {@code infrastructure} (regla
 * verificada por ArchUnit).
 */
package com.expensetracker.application.port.in;
