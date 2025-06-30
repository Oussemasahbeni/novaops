package com.novaops.userservice.domain;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.AuthUser;
import com.novaops.userservice.domain.model.Role;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.model.UserRegistrationData;
import com.novaops.userservice.domain.port.output.IdentityProvider;
import com.novaops.userservice.domain.port.output.UserRepository;
import com.novaops.userservice.domain.service.UserService;
import com.novaops.userservice.exception.ExistsException;
import com.novaops.userservice.exception.ForbiddenException;
import com.novaops.userservice.exception.NotFoundException;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository usersRepository;

  @Mock private IdentityProvider idpService;

  @InjectMocks private UserService userService;

  private User sampleUser;
  private AuthUser sampleAuthUser;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    Role sampleRole = Role.builder().id(UUID.randomUUID()).name(RoleType.USER).build();
    sampleUser =
        User.builder()
            .id(userId)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .roles(List.of(sampleRole))
            .build();

    sampleAuthUser =
        AuthUser.builder()
            .id(userId.toString())
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();
  }

  @Test
  @DisplayName("createUser should successfully create a user in IDP and database")
  void createUser_Success() {

    // Arrange
    var roles = List.of(RoleType.USER);

    var requestDto =
        new UserRegistrationData(
            "Jane", "Doe", "jane.doe@example.com", "123 Main St", "555-1234", roles);

    // When idpService.create is called, return a predefined AuthUser
    when(idpService.create(any(AuthUser.class), eq(roles), anyList(), anyList(), anyBoolean()))
        .thenReturn(
            AuthUser.builder()
                .id(userId.toString())
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .build());

    when(usersRepository.create(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    User result = userService.createUser(requestDto);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getId());
    assertEquals("Jane", result.getFirstName());
    assertEquals(requestDto.address(), result.getAddress());

    // Verify that the external ports were called correctly
    verify(idpService, times(1))
        .create(any(AuthUser.class), eq(requestDto.roles()), anyList(), anyList(), eq(true));
    verify(usersRepository, times(1)).create(any(User.class));
  }

  @Test
  @DisplayName("createUser should throw exception if email already exists")
  void createUser_EmailExists_ThrowsException() {
    // Arrange
    var roles = List.of(RoleType.USER);
    var requestDto =
        new UserRegistrationData(
            "Jane", "Doe", "jane.doe@example.com", "123 Main St", "555-1234", roles);

    when(usersRepository.existsByEmail(requestDto.email())).thenReturn(true);

    // Act & Assert
    var ex = assertThrows(ExistsException.class, () -> userService.createUser(requestDto));
    assertEquals("Email already exists", ex.getMessage());

    verify(usersRepository, never()).create(any());
    verify(idpService, never()).create(any(), any(), anyList(), anyList(), anyBoolean());
  }

  @Test
  @DisplayName("findById should return user when found")
  void findById_UserFound() {
    // Arrange
    when(usersRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

    // Act
    User result = userService.findById(userId);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getId());
  }

  @Test
  @DisplayName("findAllPaginated should delegate to repository")
  void findAllPaginated_DelegatesToRepository() {
    Pageable pageable = Pageable.unpaged();
    RoleType role = RoleType.USER;
    String search = "john";

    Page<User> page = mock(Page.class);
    when(usersRepository.findAll(search, pageable, role)).thenReturn(page);

    Page<User> result = userService.findAllPaginated(search, role, pageable);

    assertEquals(page, result);
    verify(usersRepository, times(1)).findAll(search, pageable, role);
  }

  @Test
  @DisplayName("findById should throw NotFoundException when user not found")
  void findById_UserNotFound() {
    // Arrange
    when(usersRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> {
              userService.findById(userId);
            });

    assertEquals("User not found : " + userId, exception.getMessage());
  }

  @Test
  @DisplayName("updateUser should throw NotFoundException if user to update does not exist")
  void updateUser_UserNotFound() {
    // Arrange
    when(usersRepository.findById(userId)).thenReturn(Optional.empty());
    UpdateUserRequest request = new UpdateUserRequest(userId, "oussema", "sahbeni", null, null);

    // Act & Assert
    assertThrows(NotFoundException.class, () -> userService.updateUser(userId, request));
  }

  @Test
  @DisplayName("updateUser should throw ForbiddenException if user being updated is not an admin")
  void updateUser_UserNotAdmin_Forbidden() {
    // Arrange
    when(usersRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
    UpdateUserRequest request = new UpdateUserRequest(userId, "oussema", "sahbeni", null, null);

    // Act & Assert
    assertThrows(ForbiddenException.class, () -> userService.updateUser(userId, request));
  }

  @Test
  @DisplayName("updateUser should succeed and call dependencies if user is an admin")
  void updateUser_Success() {
    // Arrange
    // Create a user who IS an admin
    Role adminRole = new Role();
    adminRole.setName(RoleType.ADMIN);
    sampleUser.setRoles(List.of(adminRole));

    UpdateUserRequest request = new UpdateUserRequest(userId, "oussema", "sahbeni", null, null);

    User updatedUserFromRepo =
        User.builder()
            .id(userId)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(sampleUser.getEmail())
            .build();

    when(usersRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
    when(usersRepository.update(request)).thenReturn(updatedUserFromRepo);
    when(idpService.update(any(AuthUser.class))).thenReturn(sampleAuthUser);

    // Use ArgumentCaptor to capture the object passed to the mock
    ArgumentCaptor<AuthUser> authUserCaptor = ArgumentCaptor.forClass(AuthUser.class);

    // Act
    User result = userService.updateUser(userId, request);

    // Assert
    assertNotNull(result);
    assertEquals("oussema", result.getFirstName());

    // Verify the idpService.update was called and capture the argument
    verify(idpService).update(authUserCaptor.capture());
    AuthUser capturedAuthUser = authUserCaptor.getValue();

    // Check that the data sent to Keycloak is correct
    assertEquals(userId.toString(), capturedAuthUser.getId());
    assertEquals("oussema", capturedAuthUser.getFirstName());
    assertEquals("sahbeni", capturedAuthUser.getLastName());

    // Verify repo calls
    verify(usersRepository, times(1)).findById(userId);
    verify(usersRepository, times(1)).update(request);
  }

  @Test
  @DisplayName("deleteById should call repository and IDP delete methods")
  void deleteById_Success() {
    // Arrange
    // No arrangement needed as methods return void

    // Act
    userService.deleteById(userId);

    // Assert
    // Verify that both delete methods were called exactly once with the correct ID
    verify(usersRepository, times(1)).deleteById(userId);
    verify(idpService, times(1)).deleteById(userId.toString());
  }
}
