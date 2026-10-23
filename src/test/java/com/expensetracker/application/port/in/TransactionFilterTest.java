package com.expensetracker.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionFilterTest {

  @Test
  void shouldAcceptMaximumPageSize() {
    TransactionFilter filter =
        new TransactionFilter(null, null, null, 0, TransactionFilter.MAX_PAGE_SIZE);

    assertThat(filter.pageSize()).isEqualTo(TransactionFilter.MAX_PAGE_SIZE);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, TransactionFilter.MAX_PAGE_SIZE + 1})
  void shouldRejectPageSizeOutOfRange(int pageSize) {
    assertThatThrownBy(() -> new TransactionFilter(null, null, null, 0, pageSize))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldRejectFilteringByCategoryAndUncategorizedAtOnce() {
    UUID categoryId = UUID.randomUUID();

    assertThatThrownBy(() -> new TransactionFilter(null, categoryId, true, null, 0, 20))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldRejectNegativePage() {
    assertThatThrownBy(() -> new TransactionFilter(null, null, null, -1, 20))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
