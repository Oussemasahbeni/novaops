package com.novaops.userservice.domain.port.output;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.AuthUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Output port interface for Identity Provider operations. Defines the contract for user identity
 * management operations with external identity providers (e.g., Keycloak) in the hexagonal
 * architecture.
 */
public interface IdentityProvider {

  /**
   * Finds a user by their unique identifier.
   *
   * @param id the unique identifier of the user
   * @return an Optional containing the user if found, empty otherwise
   */
  Optional<AuthUser> findById(String id);

  /**
   * Finds a user by their email address.
   *
   * @param email the email address of the user
   * @return an Optional containing the user if found, empty otherwise
   */
  Optional<AuthUser> findByEmail(String email);

  /**
   * Finds all users belonging to a specific group.
   *
   * @param groupId the unique identifier of the group
   * @return a list of users in the specified group
   */
  List<AuthUser> findByGroup(String groupId);

  /**
   * Retrieves all users with pagination and filtering options.
   *
   * @param roles the list of roles to filter by
   * @param search the search term for filtering users
   * @param pageable the pagination information
   * @return a page of users matching the criteria
   */
  Page<AuthUser> findAll(List<String> roles, String search, Pageable pageable);

  /**
   * Creates a new user in the identity provider.
   *
   * @param user the user to create
   * @param roleTypes the list of role types to assign
   * @param groups the list of groups to assign
   * @param requiredActions the list of required actions for the user
   * @param sendEmail whether to send a welcome email
   * @param clientId the client identifier
   * @param redirectUri the redirect URI for email links
   * @return the created user
   */
  AuthUser create(
      AuthUser user,
      List<RoleType> roleTypes,
      List<String> groups,
      List<String> requiredActions,
      boolean sendEmail);

  /**
   * Updates an existing user in the identity provider.
   *
   * @param user the user with updated information
   * @return the updated user
   */
  AuthUser update(AuthUser user);

  /**
   * Finds a user by their username.
   *
   * @param username the username of the user
   * @return an Optional containing the user if found, empty otherwise
   */
  Optional<AuthUser> findByUsername(String username);

  /**
   * Finds users by custom attribute query. Example Usage: Optional<AuthUser> user =
   * identityProvider.findByCustomAttribute("username=inspark");
   *
   * @param query the custom attribute query string
   * @return a list of users matching the custom attribute query
   */
  List<AuthUser> findByCustomAttribute(String query);

  /**
   * Finds all users with a specific role type.
   *
   * @param roleType the role type to filter by
   * @return a list of users with the specified role type
   */
  List<AuthUser> findByRole(RoleType roleType);

  /**
   * Deletes a user by their unique identifier.
   *
   * @param id the unique identifier of the user to delete
   */
  void deleteById(String id);

  /**
   * Disables a user account.
   *
   * @param id the unique identifier of the user to disable
   * @return the disabled user
   */
  AuthUser disableUser(String id);

  /**
   * Enables a user account.
   *
   * @param id the unique identifier of the user to enable
   * @return the enabled user
   */
  AuthUser enableUser(String id);

  /**
   * Toggles the enabled/disabled status of a user.
   *
   * @param userId the unique identifier of the user
   * @return the user with toggled status
   */
  AuthUser toggle(String userId);

  /**
   * Updates the locale preference for a user.
   *
   * @param userId the unique identifier of the user
   * @param locale the new locale preference
   * @return the user with updated locale
   */
  AuthUser updateLocale(String userId, Locale locale);
}
