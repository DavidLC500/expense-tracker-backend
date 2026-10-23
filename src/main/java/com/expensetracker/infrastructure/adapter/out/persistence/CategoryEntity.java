package com.expensetracker.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Entidad JPA de {@code categories}. */
@Entity
@Table(name = "categories")
public class CategoryEntity {

  @Id private UUID id;

  private String name;

  protected CategoryEntity() {}

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
