package com.expensetracker.application.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotFoundExceptionTest {

  private static final UUID ID = UUID.fromString("7f1c2a3b-0000-4000-8000-000000000001");

  @Test
  void shouldBuildTheSameMessageForEveryResource() {
    assertThat(new CategoryNotFoundException(ID))
        .hasMessage("Category not found with id " + ID)
        .isInstanceOf(NotFoundException.class);
    assertThat(new RuleNotFoundException(ID)).hasMessage("Rule not found with id " + ID);
    assertThat(new TransactionNotFoundException(ID))
        .hasMessage("Transaction not found with id " + ID);
  }
}
