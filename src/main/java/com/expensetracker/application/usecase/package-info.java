/**
 * Implementaciones de los casos de uso: una clase por caso de uso, cada una implementando una
 * interfaz de {@code application.port.in}.
 *
 * <p>Java puro: este paquete no puede depender de Spring, JPA ni de {@code infrastructure} (regla
 * verificada por ArchUnit). Las clases se registran como {@code @Bean} desde {@code
 * infrastructure.config}.
 */
package com.expensetracker.application.usecase;
