package com.expensetracker.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Entidad JPA de {@code transactions}. */
@Entity
@Table(name = "transactions")
public class TransactionEntity {

  @Id private UUID id;

  private LocalDate date;

  private String concept;

  private BigDecimal amount;

  private String currency;

  @Column(name = "category_id")
  private UUID categoryId;

  @Column(name = "import_hash")
  private String importHash;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "manually_categorized")
  private boolean manuallyCategorized;

  protected TransactionEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getConcept() {
    return concept;
  }

  public void setConcept(String concept) {
    this.concept = concept;
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

  public UUID getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(UUID categoryId) {
    this.categoryId = categoryId;
  }

  public String getImportHash() {
    return importHash;
  }

  public void setImportHash(String importHash) {
    this.importHash = importHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public boolean isManuallyCategorized() {
    return manuallyCategorized;
  }

  public void setManuallyCategorized(boolean manuallyCategorized) {
    this.manuallyCategorized = manuallyCategorized;
  }
}
