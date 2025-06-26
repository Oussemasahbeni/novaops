package com.novaops.userservice.config.init;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.infrastructure.entity.RoleEntity;
import com.novaops.userservice.infrastructure.entity.UserEntity;
import com.novaops.userservice.infrastructure.repository.UserJpaRepository;
import com.novaops.userservice.infrastructure.utils.KeycloakUtils;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.*;
import lombok.extern.log4j.Log4j2;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CreateDefaultUser {

  @Value("${keycloak.realm}")
  public String realm;

  @Value("${keycloak.default-user-password}")
  public String defaultUserPassword;

  public void createDefaultAdmin(Keycloak keycloak, UserJpaRepository userRepository) {
    Locale locale = Locale.EN;
    String adminPhoneNumber = "54 750 526";
    String adminEmail = "oussemasahbeni300@gmail.com";
    String adminFirstName = "oussema";
    String adminLastName = "sahbeni";

    List<UserRepresentation> keycloakAdminUsers =
        keycloak.realm(realm).users().searchByEmail(adminEmail, true);
    if (!keycloakAdminUsers.isEmpty()) {
      log.info("Admin user found in Keycloak, checking database...");
      Optional<UserEntity> existingAdminInDb = userRepository.findByEmail(adminEmail);
      if (existingAdminInDb.isPresent()) {
        log.info("Admin user already exists in Keycloak and Database.");
      } else {
        log.info("Admin user found in Keycloak but not in Database, adding to Database.");
        UserRepresentation adminRepresentation = keycloakAdminUsers.getFirst();
        UserEntity defaultAdmin =
            UserEntity.builder()
                .id(UUID.fromString(adminRepresentation.getId()))
                .email(adminEmail)
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .locale(locale)
                .phoneNumber(adminPhoneNumber)
                .build();
        RoleEntity roleEntity =
            RoleEntity.builder().name(RoleType.ADMIN).user(defaultAdmin).build();
        if (defaultAdmin.getRoles() == null) {
          defaultAdmin.setRoles(new ArrayList<>());
        }

        defaultAdmin.getRoles().add(roleEntity);
        userRepository.save(defaultAdmin);
        log.info("Admin user added to database from existing Keycloak user.");
      }

    } else {
      log.info("Admin user not found in Keycloak, creating...");
      try {
        UserRepresentation adminRepresentation = new UserRepresentation();
        adminRepresentation.setEmail(adminEmail);
        adminRepresentation.setFirstName(adminFirstName);
        adminRepresentation.setLastName(adminLastName);
        adminRepresentation.setEnabled(true);
        adminRepresentation.setEmailVerified(true);
        adminRepresentation.setRequiredActions(Collections.singletonList("UPDATE_PASSWORD"));
        CredentialRepresentation adminCredentialRepresentation =
            KeycloakUtils.createPasswordCredentials(defaultUserPassword, true);
        adminRepresentation.setCredentials(
            Collections.singletonList(adminCredentialRepresentation));
        Response adminResponse = keycloak.realm(realm).users().create(adminRepresentation);
        String adminUserId = CreatedResponseUtil.getCreatedId(adminResponse);
        RoleRepresentation adminRole =
            keycloak.realm(realm).roles().get(String.valueOf(RoleType.ADMIN)).toRepresentation();
        List<RoleRepresentation> adminRoles = List.of(adminRole);
        keycloak.realm(realm).users().get(adminUserId).roles().realmLevel().add(adminRoles);
        UserEntity defaultAdmin =
            UserEntity.builder()
                .id(UUID.fromString(adminUserId))
                .email(adminEmail)
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .locale(locale)
                .phoneNumber(adminPhoneNumber)
                .build();
        RoleEntity roleEntity =
            RoleEntity.builder().name(RoleType.ADMIN).user(defaultAdmin).build();
        if (defaultAdmin.getRoles() == null) {
          defaultAdmin.setRoles(new ArrayList<>());
        }

        defaultAdmin.getRoles().add(roleEntity);
        userRepository.save(defaultAdmin);
        log.info("Admin user created in Keycloak and Database.");
      } catch (WebApplicationException e) {
        log.error("Error creating default admin user", e);
      }
    }
  }
}
