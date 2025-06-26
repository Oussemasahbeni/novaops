package com.novaops.userservice.domain.port.input;


import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.AuthUser;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;

public interface IdentityProviderUseCases {

  /**
   * Updates an existing user record.
   *
   * @param authUser The updated user details.
   * @return The updated user.
   */
  AuthUser update(AuthUser authUser);

  /**
   * Retrieves a user by their ID.
   *
   * @param id The user's ID.
   * @return The requested user.
   */
  AuthUser findById(String id);

  AuthUser findByEmail(String email);

  Optional<AuthUser> findByUsername(String username);

  /**
   * Example Usage: List<AuthUser> user =
   * identityProvider.findByCustomAttribute("username=inspark");
   */
  List<AuthUser> findByCustomAttribute(String query);

  List<AuthUser> findByRole(RoleType roleType);

  List<AuthUser> findByGroup(String role);

  /**
   * Retrieves all users with specified roles.
   *
   * @param roles The roles to filter users by.
   * @return A Page of users with the specified roles.
   */
  Page<AuthUser> findAll(List<String> roles, String search, Pageable pageable);

  /**
   * Enables a user's active status.
   *
   * @param userId The ID of the user to enable.
   * @return The user with the enabled status.
   */
  AuthUser enableUser(String userId);

  /**
   * Disables a user's active status.
   *
   * @param userId The ID of the user to disable.
   * @return The user with the disabled status.
   */
  AuthUser disableUser(String userId);

  /**
   * Deletes a user from the system.
   *
   * @param userId The ID of the user to delete.
   */
  void deleteById(String userId);

  AuthUser toggleUserStatus(String userId);

  AuthUser updateLocale(
      String name, Collection<? extends GrantedAuthority> authorities, Locale locale);
}
