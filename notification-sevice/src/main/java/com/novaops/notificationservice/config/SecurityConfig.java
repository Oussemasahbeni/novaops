package com.novaops.notificationservice.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {

  private static final String[] AUTH_WHITELIST = {
    "/v3/api-docs/**",
    "/swagger-resources/**",
    "/configuration/ui",
    "/configuration/security",
    "/docs/**",
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/webjars/**",
    "/docs.html",
    "/swagger-ui/index.html",
    "/eureka/**"
  };

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(List.of("http://localhost:8222"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowedMethods(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
