package com.novaops.userservice.domain.service;

import com.novaops.userservice.domain.enums.NotificationPreference;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.AuthUser;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.model.UserRegistrationData;
import com.novaops.userservice.domain.model.csv.CsvImportOptions;
import com.novaops.userservice.domain.model.csv.CsvValidationResult;
import com.novaops.userservice.domain.port.input.UserUseCases;
import com.novaops.userservice.domain.port.output.IdentityProvider;
import com.novaops.userservice.domain.port.output.UserCsvLoader;
import com.novaops.userservice.domain.port.output.UserRepository;
import com.novaops.userservice.exception.ExistsException;
import com.novaops.userservice.exception.ForbiddenException;
import com.novaops.userservice.exception.GenericException;
import com.novaops.userservice.exception.NotFoundException;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import com.novaops.userservice.shared.annotation.DomainService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@DomainService
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserUseCases {

  private final UserRepository users;
  private final IdentityProvider idpService;
  private final UserCsvLoader userCsvLoader;

  @Override
  public User createUser(UserRegistrationData requestDto) {

    if (Boolean.TRUE.equals(users.existsByEmail(requestDto.email()))) {
      throw new ExistsException(
          ExistsException.ExistsExceptionType.EMAIL_ALREADY_EXISTS, requestDto.email());
    }
    List<String> groups = List.of();
    List<String> requiredActions = List.of("UPDATE_PASSWORD");
    AuthUser authUser =
        AuthUser.builder()
            .firstName(requestDto.firstName())
            .lastName(requestDto.lastName())
            .email(requestDto.email())
            .enabled(true)
            .emailVerified(false)
            .build();
    AuthUser createdUser =
        idpService.create(authUser, requestDto.roles(), groups, requiredActions, true);

    try {
      User user =
          User.builder()
              .id(UUID.fromString(createdUser.getId()))
              .firstName(createdUser.getFirstName())
              .lastName(createdUser.getLastName())
              .email(createdUser.getEmail())
              .address(requestDto.address())
              .phoneNumber(requestDto.phoneNumber())
              .profilePicture(null)
              .address(requestDto.address())
              .locale(null)
              .notificationPreference(NotificationPreference.ALL)
              .build();
      user.setRoles(user.setRolesFromRoleType(requestDto.roles()));
      return users.create(user);
    } catch (Exception e) {
      idpService.deleteById(createdUser.getId());
      throw new GenericException(
          GenericException.GenericExceptionType.FAILED_TO_SAVE_USER, requestDto.email());
    }
  }

  @Override
  public CsvValidationResult importUsers(MultipartFile file, CsvImportOptions options) {

    try {
      CsvValidationResult validationResult = userCsvLoader.parseAndValidateCsvUsers(file, options);

      if (validationResult.getErrors().isEmpty() || options.isSkipInvalidRows()) {}

      return validationResult;

    } catch (Exception e) {
      log.error("Error importing users from CSV: {}", e.getMessage());
      throw new GenericException(
          GenericException.GenericExceptionType.FAILED_TO_IMPORT_USERS, e.getMessage());
    }
  }

  @Override
  public Boolean existsByEmail(String email) {
    return users.existsByEmail(email);
  }

  @Override
  public Page<User> findAllPaginated(String search, RoleType role, Pageable pageable) {
    return users.findAll(search, pageable, role);
  }

  @Override
  public User findById(UUID id) {
    return users
        .findById(id)
        .orElseThrow(
            () ->
                new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND, id));
  }

  @Override
  public User updateUser(UUID id, UpdateUserRequest updateUserRequest) {

    User user =
        users
            .findById(id)
            .orElseThrow(
                () ->
                    new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
    if (user.getRoles().stream().noneMatch(role -> role.getName().equals(RoleType.ADMIN))) {
      throw new ForbiddenException(ForbiddenException.ForbiddenExceptionType.ACTION_NOT_ALLOWED);
    }
    User updatedUser = users.update(updateUserRequest);
    AuthUser authUser = new AuthUser();
    // This is needed to update the user in Keycloak
    authUser.setId(user.getId().toString());
    authUser.setFirstName(updatedUser.getFirstName());
    authUser.setLastName(updatedUser.getLastName());
    idpService.update(authUser);
    return updatedUser;
  }

  @Override
  public void deleteById(UUID id) {
    users.deleteById(id);
    idpService.deleteById(String.valueOf(id));
  }

  @Override
  public User findCurrentUser(UUID id) {

    return users
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
  }
}
