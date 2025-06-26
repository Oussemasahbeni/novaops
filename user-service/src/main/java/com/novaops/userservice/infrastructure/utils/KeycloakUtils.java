package com.novaops.userservice.infrastructure.utils;

import org.keycloak.representations.idm.CredentialRepresentation;

public class KeycloakUtils {

  private KeycloakUtils() {}

  public static CredentialRepresentation createPasswordCredentials(
      String password, boolean temporary) {
    CredentialRepresentation passwordCredentials = new CredentialRepresentation();
    passwordCredentials.setTemporary(temporary);
    passwordCredentials.setType(CredentialRepresentation.PASSWORD);
    passwordCredentials.setValue(password);
    return passwordCredentials;
  }
}
