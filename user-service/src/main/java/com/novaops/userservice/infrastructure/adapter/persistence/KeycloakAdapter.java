package com.novaops.userservice.infrastructure.adapter.persistence;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.AuthUser;
import com.novaops.userservice.domain.port.output.IdentityProvider;
import com.novaops.userservice.exception.BadRequestException;
import com.novaops.userservice.exception.GenericException;
import com.novaops.userservice.exception.NotFoundException;
import com.novaops.userservice.infrastructure.mapper.AuthMapper;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Make sure to enable <b>view-realm</b> and <b>manage-users</b> roles for the client service
 * account in keycloak
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdapter implements IdentityProvider {

  private final Keycloak keycloak;
  private final AuthMapper authMapper;

  private static final String frontendClientId = "novaops-frontend";

  @Value("${keycloak.realm}")
  public String realm;

  @Override
  public Optional<AuthUser> findById(String id) {
    UserRepresentation userRepresentation =
        keycloak.realm(realm).users().get(id).toRepresentation();

    if (userRepresentation == null) {
      return Optional.empty();
    }
    return Optional.of(mapUser(userRepresentation));
  }

  @Override
  public List<AuthUser> findByGroup(String groupName) {

    List<GroupRepresentation> groups = keycloak.realm(realm).groups().groups();
    GroupRepresentation group =
        groups.stream().filter(g -> g.getName().equals(groupName)).findFirst().orElseThrow();

    List<UserRepresentation> users = keycloak.realm(realm).groups().group(group.getId()).members();
    List<AuthUser> authUsers = new ArrayList<>();
    for (UserRepresentation user : users) {
      AuthUser mappedUser = mapUser(user);
      authUsers.add(mappedUser);
    }
    return authUsers;
  }

  @Override
  public Page<AuthUser> findAll(List<String> roles, String search, Pageable pageable) {

    RealmResource realmResource = keycloak.realm(realm);

    List<UserRepresentation> users =
        realmResource.users().search(search, pageable.getPageNumber(), pageable.getPageSize());
    List<AuthUser> authUsers = new ArrayList<>();
    for (UserRepresentation user : users) {
      processUserRoles(user, roles, authUsers, realmResource);
    }
    sortAuthUsers(authUsers, pageable.getSort());
    int totalElements = realmResource.users().count();
    return new PageImpl<>(authUsers, pageable, totalElements);
  }

  /** Process user roles and add to the list of auth users if the user has the required roles */
  private void processUserRoles(
      UserRepresentation user,
      List<String> roles,
      List<AuthUser> authUsers,
      RealmResource realmResource) {
    List<RoleRepresentation> userRoles =
        realmResource.users().get(user.getId()).roles().realmLevel().listEffective();
    List<RoleType> roleTypeEnums = new ArrayList<>();
    for (RoleRepresentation roleRepresentation : userRoles) {
      if (roleRepresentation.getName().equals("default-roles-" + realm)) {
        Set<RoleRepresentation> defaultRoles =
            realmResource.rolesById().getRoleComposites(roleRepresentation.getId());
        for (RoleRepresentation defaultRole : defaultRoles) {
          roleTypeEnums.add(RoleType.fromString(defaultRole.getName()).orElse(null));
        }
      } else {
        roleTypeEnums.add(RoleType.fromString(roleRepresentation.getName()).orElse(null));
      }
    }
    roleTypeEnums = roleTypeEnums.stream().filter(Objects::nonNull).toList();

    if (!roles.isEmpty()
        && roleTypeEnums.stream().anyMatch(role -> roles.contains(role.toString()))) {
      AuthUser mappedUser = mapUser(user);
      authUsers.add(mappedUser);
    }
  }

  private void sortAuthUsers(List<AuthUser> authUsers, Sort sort) {
    if (sort.isSorted()) {
      sort.forEach(
          order -> {
            Comparator<AuthUser> comparator;
            switch (order.getProperty()) {
              case "email" -> comparator = Comparator.comparing(AuthUser::getEmail);
              case "firstName" -> comparator = Comparator.comparing(AuthUser::getFirstName);
              case "lastName" -> comparator = Comparator.comparing(AuthUser::getLastName);
              case "createdAt" -> comparator = Comparator.comparing(AuthUser::getCreatedAt);
              default -> comparator = Comparator.comparing(AuthUser::getId);
            }
            if (order.getDirection() == Sort.Direction.DESC) {
              comparator = comparator.reversed();
            }
            authUsers.sort(comparator);
          });
    }
  }

  @Override
  public Optional<AuthUser> findByEmail(String email) {
    List<UserRepresentation> users = keycloak.realm(realm).users().searchByEmail(email, true);
    return getAuthUser(users);
  }

  @NotNull
  private Optional<AuthUser> getAuthUser(List<UserRepresentation> users) {
    if (!users.isEmpty()) {
      var user = users.getFirst();
      return Optional.of(this.mapUser(user));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<AuthUser> findByUsername(String username) {
    List<UserRepresentation> users = keycloak.realm(realm).users().searchByUsername(username, true);
    return getAuthUser(users);
  }

  /**
   * Example Usage: Optional<AuthUser> user =
   * identityProvider.findByCustomAttribute("username=inspark");
   */
  @Override
  public List<AuthUser> findByCustomAttribute(String query) {
    List<UserRepresentation> users = keycloak.realm(realm).users().searchByAttributes(query);
    List<AuthUser> authUsers = new ArrayList<>();
    for (UserRepresentation user : users) {
      AuthUser mappedUser = mapUser(user);
      authUsers.add(mappedUser);
    }
    return authUsers;
  }

  /**
   * This method requires the role <b>view-realm</b> to be enabled for the client service accounts
   * roles in keycloak
   */
  @Override
  public List<AuthUser> findByRole(RoleType roleType) {
    List<UserRepresentation> users =
        keycloak.realm(realm).roles().get(roleType.name()).getUserMembers();
    List<AuthUser> authUsers = new ArrayList<>();
    for (UserRepresentation user : users) {
      AuthUser mappedUser = mapUser(user);
      authUsers.add(mappedUser);
    }
    return authUsers;
  }

  @Override
  public void deleteById(String id) {
    log.info("Deleting user with id: {}", id);
    try {
      keycloak.realm(realm).users().delete(id);
      log.info("User with id: {} deleted successfully", id);
    } catch (WebApplicationException e) {
      log.error("Error deleting user with id: {}", id, e);
      throw new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND);
    }
  }

  @Override
  public AuthUser create(
      AuthUser authUser,
      List<RoleType> roleTypes,
      List<String> groups,
      List<String> requiredActions,
      boolean sendUpdatePasswordEmail) {
    log.info("Creating user with email: {}", authUser.getEmail());
    try {
      UserRepresentation userRepresentation = authMapper.toUserRepresentation(authUser);
      userRepresentation.setAttributes(authUser.getAttributes());
      userRepresentation.setGroups(groups);
      userRepresentation.setRequiredActions(requiredActions);
      Response response = keycloak.realm(realm).users().create(userRepresentation);
      String userId = CreatedResponseUtil.getCreatedId(response);
      if (roleTypes != null) {
        roleTypes.forEach(
            role -> {
              RoleRepresentation roleRepresentation =
                  keycloak.realm(realm).roles().get(role.toString()).toRepresentation();
              keycloak
                  .realm(realm)
                  .users()
                  .get(userId)
                  .roles()
                  .realmLevel()
                  .add(Collections.singletonList(roleRepresentation));
            });
      }

      if (sendUpdatePasswordEmail) {
        try {
          ClientRepresentation clientRepresentation =
              keycloak.realm(realm).clients().findByClientId(frontendClientId).getFirst();
          keycloak
              .realm(realm)
              .users()
              .get(userId)
              .executeActionsEmail(
                  clientRepresentation.getClientId(),
                  clientRepresentation.getRootUrl(),
                  Collections.singletonList("UPDATE_PASSWORD"));
        } catch (WebApplicationException e) {
          log.error(
              "Error sending update password email to user with email: {}", authUser.getEmail(), e);
          throw new GenericException(
              GenericException.GenericExceptionType.FAILED_TO_IDP_SEND_EMAIL);
        }
      }

      log.info("User with email: {} created successfully", authUser.getEmail());

      return this.findById(userId)
          .orElseThrow(
              () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
    } catch (WebApplicationException e) {
      log.error("Error creating user with email: {}", authUser.getEmail(), e);
      throw new BadRequestException(BadRequestException.BadRequestExceptionType.INVALID_REQUEST);
    }
  }

  @Override
  public AuthUser update(AuthUser user) {
    AuthUser oldUser =
        this.findById(user.getId())
            .orElseThrow(
                () ->
                    new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));

    AuthUser newUser = authMapper.partialUpdate(user, oldUser);
    UserRepresentation userRepresentation = authMapper.toUserRepresentation(newUser);
    // TODO: update this
    //        if (user.getLocale() != null) {
    //            userRepresentation.singleAttribute("locale", user.getLocale().name());
    //        }
    //        if (user.getPhoneNumber() != null) {
    //            userRepresentation.singleAttribute("phoneNumber", user.getPhoneNumber());
    //        }
    //        if (user.getProfilePicture() != null) {
    //            userRepresentation.singleAttribute("profilePicture", user.getProfilePicture());
    //        }
    keycloak.realm(realm).users().get(user.getId()).update(userRepresentation);

    return this.findById(user.getId())
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }

  @Override
  public AuthUser enableUser(String userId) {
    UserRepresentation userRepresentation =
        keycloak.realm(realm).users().get(userId).toRepresentation();
    userRepresentation.setEnabled(true);
    keycloak.realm(realm).users().get(userRepresentation.getId()).update(userRepresentation);
    UserRepresentation userRepresentationUpdated =
        keycloak.realm(realm).users().get(userRepresentation.getId()).toRepresentation();
    return authMapper.toAuthUser(userRepresentationUpdated);
  }

  @Override
  public AuthUser disableUser(String userId) {
    UserRepresentation userRepresentation =
        keycloak.realm(realm).users().get(userId).toRepresentation();
    userRepresentation.setEnabled(false);
    keycloak.realm(realm).users().get(userRepresentation.getId()).update(userRepresentation);
    UserRepresentation userRepresentationUpdated =
        keycloak.realm(realm).users().get(userRepresentation.getId()).toRepresentation();
    return authMapper.toAuthUser(userRepresentationUpdated);
  }

  @Override
  public AuthUser toggle(String userId) {
    UserRepresentation userRepresentation =
        keycloak.realm(realm).users().get(userId).toRepresentation();
    userRepresentation.setEnabled(!userRepresentation.isEnabled());
    keycloak.realm(realm).users().get(userRepresentation.getId()).update(userRepresentation);
    UserRepresentation userRepresentationUpdated =
        keycloak.realm(realm).users().get(userRepresentation.getId()).toRepresentation();
    return authMapper.toAuthUser(userRepresentationUpdated);
  }

  @Override
  public AuthUser updateLocale(String userId, Locale locale) {
    UserRepresentation userRepresentation =
        keycloak.realm(realm).users().get(userId).toRepresentation();
    userRepresentation.singleAttribute("locale", locale.name());
    keycloak.realm(realm).users().get(userRepresentation.getId()).update(userRepresentation);
    UserRepresentation userRepresentationUpdated =
        keycloak.realm(realm).users().get(userRepresentation.getId()).toRepresentation();
    return authMapper.toAuthUser(userRepresentationUpdated);
  }

  private List<RoleType> getUserRoles(String userId) {
    List<RoleRepresentation> realmRoles =
        keycloak.realm(realm).users().get(userId).roles().realmLevel().listEffective();
    List<String> roleNames = RoleType.getAllRoleNames();
    return realmRoles.stream()
        .map(RoleRepresentation::getName)
        .filter(roleNames::contains)
        .map(RoleType::valueOf)
        .toList();
  }

  private AuthUser mapUser(UserRepresentation userRepresentation) {
    List<RoleType> roleTypes = getUserRoles(userRepresentation.getId());
    AuthUser authUser = authMapper.toAuthUser(userRepresentation);
    //        String locale = userRepresentation.getAttributes().getOrDefault("locale",
    // List.of(DEFAULT_LOCALE)).stream()
    //                .findFirst()
    //                .orElse(null);
    //        authUser.setLocale(Locale.valueOf(locale));
    //        authUser.setPhoneNumber(userRepresentation.getAttributes().getOrDefault("phoneNumber",
    // List.of()).stream()
    //                .findFirst()
    //                .orElse(null));
    //
    // authUser.setProfilePicture(userRepresentation.getAttributes().getOrDefault("profilePicture",
    // List.of()).stream()
    //                .findFirst()
    //                .orElse(null));

    authUser.setRoleTypes(roleTypes);
    return authUser;
  }
}
