package com.expensetracker.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Entidad JPA de {@code budgets}. */
@Entity
@Table(name = "budgets")
public class BudgetEntity {

  @Id private UUID id;

  @Column(name = "category_id")
  private UUID categoryId;

  private LocalDate month;

  private BigDecimal amount;

  private String currency;

  protected BudgetEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  public LocalDate getMonth() {
    return month;
  }

  public void setMonth(LocalDate month) {
    this.month = month;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}
