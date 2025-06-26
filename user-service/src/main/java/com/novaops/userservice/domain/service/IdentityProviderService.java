package com.novaops.userservice.domain.service;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.AuthUser;
import com.novaops.userservice.domain.port.input.IdentityProviderUseCases;
import com.novaops.userservice.domain.port.output.IdentityProvider;
import com.novaops.userservice.domain.port.output.UserRepository;
import com.novaops.userservice.exception.NotFoundException;
import com.novaops.userservice.shared.annotation.DomainService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;

@DomainService
@Slf4j
@RequiredArgsConstructor
public class IdentityProviderService implements IdentityProviderUseCases {

  private final IdentityProvider iamUserRepository;
  private final UserRepository users;

  @Override
  public AuthUser update(AuthUser authUser) {
    return iamUserRepository.update(authUser);
  }

  @Override
  public AuthUser findById(String id) {
    return iamUserRepository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }

  @Override
  public AuthUser findByEmail(String email) {
    return iamUserRepository
        .findByEmail(email)
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }

  @Override
  public Optional<AuthUser> findByUsername(String username) {
    return iamUserRepository.findByUsername(username);
  }

  /**
   * Example Usage: Optional<AuthUser> user =
   * identityProvider.findByCustomAttribute("username=inspark");
   */
  @Override
  public List<AuthUser> findByCustomAttribute(String query) {
    return iamUserRepository.findByCustomAttribute(query);
  }

  @Override
  public List<AuthUser> findByRole(RoleType roleType) {
    return iamUserRepository.findByRole(roleType);
  }

  @Override
  public List<AuthUser> findByGroup(String role) {
    return iamUserRepository.findByGroup(role);
  }

  @Override
  public Page<AuthUser> findAll(List<String> roles, String search, Pageable pageable) {
    return iamUserRepository.findAll(roles, search, pageable);
  }

  /**
   * Enables a user's active status.
   *
   * @param userId The ID of the user to enable.
   * @return The user with the enabled status.
   */
  @Override
  public AuthUser enableUser(String userId) {
    return this.iamUserRepository.enableUser(userId);
  }

  /**
   * Disables a user's active status.
   *
   * @param userId The ID of the user to disable.
   * @return The user with the disabled status.
   */
  @Override
  public AuthUser disableUser(String userId) {
    return this.iamUserRepository.disableUser(userId);
  }

  //  @Override
  //  public AuthUser toggleUserStatus(String userId) {
  //    return iamUserRepository.toggle(userId);
  //  }

  @Override
  public void deleteById(String userId) {
    iamUserRepository.deleteById(userId);
  }

  public AuthUser toggleUserStatus(String userId) {
    return iamUserRepository.toggle(userId);
  }

  @Override
  public AuthUser updateLocale(
      String userId, Collection<? extends GrantedAuthority> authorities, Locale locale) {
    AuthUser user = iamUserRepository.updateLocale(userId, locale);
    users.updateLocale(user.getId(), locale);
    return user;
  }
}
