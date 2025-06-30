package com.novaops.gateway.config;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
  /**
   * see : <a
   * href="https://www.netjstech.com/2023/11/spring-boot-microservice-circuit-breaker-resilience4j.html">...</a>
   *
   * @return
   */
  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory ->
        factory.configureDefault(
            id ->
                new Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(
                        TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(3000)).build())
                    .circuitBreakerConfig(
                        CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .slidingWindowType(COUNT_BASED)
                            .permittedNumberOfCallsInHalfOpenState(3)
                            .failureRateThreshold(50.0F)
                            .waitDurationInOpenState(Duration.ofSeconds(5))
                            .slowCallDurationThreshold(Duration.ofMillis(200))
                            .slowCallRateThreshold(50.0F)
                            .automaticTransitionFromOpenToHalfOpenEnabled(true)
                            .minimumNumberOfCalls(5)
                            .build())
                    .build());
  }


}
