package com.novaops.userservice.config.openapi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.open-api")
public class OpenApiConfigurationProperties {

  /**
   * Determines whether Swagger v3 API documentation and related endpoints are accessible bypassing
   * Authentication and Authorization checks. Swagger endpoints are restricted by default.
   *
   * <p>Can be used in profile-specific configuration files to control access based on current
   * environments.
   */
  private boolean enabled;

  private String title;
  private String description;
  private String apiVersion;
}
