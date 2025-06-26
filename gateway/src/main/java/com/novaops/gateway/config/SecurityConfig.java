package com.novaops.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(value = {GlobalCorsProperties.class})
public class SecurityConfig {

  private static final String[] AUTH_WHITELIST = {
    // Gateway's own API docs
    "/v3/api-docs/**",
    "/docs/**",
    "/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html",

    // Generic pattern for all microservices API docs
    "/*/v3/api-docs/**",
    "/*/swagger-ui/**",
    "/*/swagger-ui.html",

    // Other endpoints
    "/eureka/**",

    // Health and actuator endpoints (optional)
    "/actuator/**",
    "/*/actuator/**"
  };

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
    serverHttpSecurity
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchange ->
                exchange.pathMatchers(AUTH_WHITELIST).permitAll().anyExchange().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    return serverHttpSecurity.build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @RefreshScope
  public CorsWebFilter corsWebFilter(CorsConfigurationSource corsConfigurationSource) {
    return new CorsWebFilter(corsConfigurationSource);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      GlobalCorsProperties globalCorsProperties) {
    var source = new UrlBasedCorsConfigurationSource();
    globalCorsProperties.getCorsConfigurations().forEach(source::registerCorsConfiguration);
    return source;
  }

  @PostConstruct
  public void postConstruct() {
    System.out.println("Starting ApiGatewaySecurityAutoConfiguration");
  }
}
