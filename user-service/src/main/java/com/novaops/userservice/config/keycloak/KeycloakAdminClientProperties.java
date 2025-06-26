package com.novaops.userservice.config.keycloak;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "keycloak")
@KeycloakConnectionValid
public class KeycloakAdminClientProperties {

  @NotBlank(message = "Keycloak admin client server URL must be configured")
  private String url;

  @NotBlank(message = "Keycloak admin client realm must be configured")
  private String realm;

  @NotBlank(message = "Keycloak admin client ID must be configured")
  private String clientId;

  @NotBlank(message = "Keycloak admin client secret must be configured")
  private String clientSecret;
}
