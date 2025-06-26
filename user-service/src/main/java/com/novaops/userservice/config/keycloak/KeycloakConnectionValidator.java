package com.novaops.userservice.config.keycloak;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeycloakConnectionValidator
    implements ConstraintValidator<KeycloakConnectionValid, KeycloakAdminClientProperties> {

  private static final Logger log = LoggerFactory.getLogger(KeycloakConnectionValidator.class);

  @Override
  public void initialize(KeycloakConnectionValid constraintAnnotation) {}

  @Override
  public boolean isValid(
      KeycloakAdminClientProperties properties, ConstraintValidatorContext context) {
    if (properties == null
        || isNullOrBlank(properties.getUrl())
        || isNullOrBlank(properties.getRealm())
        || isNullOrBlank(properties.getClientId())
        || isNullOrBlank(properties.getClientSecret())) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Keycloak admin client properties (serverUrl, realm, clientId, clientSecret) must not be blank.")
          .addConstraintViolation();
      return false;
    }

    Keycloak keycloak = null;
    try {
      log.info(
          "Attempting to validate Keycloak admin client connection to server: {}, realm: {}",
          properties.getUrl(),
          properties.getRealm());

      keycloak =
          KeycloakBuilder.builder()
              .serverUrl(properties.getUrl())
              .realm(properties.getRealm())
              .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
              .clientId(properties.getClientId())
              .clientSecret(properties.getClientSecret())
              .build();

      // Perform a lightweight operation to test the connection.
      // Getting server info is a good choice.
      keycloak.serverInfo().getInfo();

      log.info(
          "Successfully validated Keycloak admin client connection for realm '{}'.",
          properties.getRealm());
      return true;
    } catch (WebApplicationException | ProcessingException e) {
      log.warn("Keycloak admin client connection validation failed: {}", e.getMessage());
      // Customize the error message reported by the validation framework
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Failed to connect to Keycloak or validate credentials: %s. URL: %s, Realm: %s, ClientID: %s. Check Keycloak server status and configuration.",
                  e.getMessage(),
                  properties.getUrl(),
                  properties.getRealm(),
                  properties.getClientId()))
          .addConstraintViolation();
      return false;
    } catch (Exception e) {
      log.error(
          "Unexpected error during Keycloak admin client connection validation: {}",
          e.getMessage(),
          e);
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format("Unexpected error during Keycloak validation: %s", e.getMessage()))
          .addConstraintViolation();
      return false;
    } finally {
      if (keycloak != null) {
        keycloak.close();
      }
    }
  }

  private boolean isNullOrBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
