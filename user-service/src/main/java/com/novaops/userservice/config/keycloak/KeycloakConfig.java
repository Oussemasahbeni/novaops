package com.novaops.userservice.config.keycloak;

import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KeycloakAdminClientProperties.class)
public class KeycloakConfig {

  private final KeycloakAdminClientProperties properties;

  /**
   * this bean creates a Keycloak object. The Keycloak object is used to interact with Keycloak
   * admin services.
   */
  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .grantType(CLIENT_CREDENTIALS)
        .serverUrl(properties.getUrl())
        .realm(properties.getRealm())
        .clientId(properties.getClientId())
        .clientSecret(properties.getClientSecret())
        .build();
  }
}
