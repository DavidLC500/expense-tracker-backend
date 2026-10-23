package com.expensetracker.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Expone {@link Clock#systemUTC()} como bean, para poder sustituirlo por uno fijo en tests. */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
