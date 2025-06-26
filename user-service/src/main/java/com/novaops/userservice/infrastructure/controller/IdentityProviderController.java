package com.novaops.userservice.infrastructure.controller;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.AuthUser;
import com.novaops.userservice.domain.port.input.IdentityProviderUseCases;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import com.novaops.userservice.infrastructure.dto.response.AuthUserDto;
import com.novaops.userservice.infrastructure.mapper.AuthMapper;
import com.novaops.userservice.shared.pagination.CustomPage;
import com.novaops.userservice.shared.pagination.PageMapper;
import com.novaops.userservice.shared.pagination.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/idp")
@RequiredArgsConstructor
@Tag(
    name = "Identity Provider",
    description = "Endpoints for managing users and identity information")
public class IdentityProviderController {

  private final IdentityProviderUseCases identityProviderUseCases;
  private final AuthMapper authMapper;

  /**
   * Retrieves a user by ID.
   *
   * @param userId the user ID
   * @return the user
   */
  @Operation(summary = "Get user by ID", description = "Retrieves a user based on their unique ID.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/{userId}")
  public ResponseEntity<AuthUserDto> getById(
      @Parameter(description = "ID of the user to be retrieved", required = true)
          @PathVariable("userId")
          String userId) {
    AuthUser user = this.identityProviderUseCases.findById(userId);
    return new ResponseEntity<>(authMapper.toUserDto(user), HttpStatus.OK);
  }

  @Operation(
      summary = "Get current user",
      description = "Retrieves the currently authenticated user's information.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/me")
  public ResponseEntity<AuthUserDto> getMe() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    AuthUser user = this.identityProviderUseCases.findById(authentication.getName());
    return new ResponseEntity<>(authMapper.toUserDto(user), HttpStatus.OK);
  }

  /**
   * Retrieves a user by email.
   *
   * @param email the user email
   * @return the user
   */
  @Operation(
      summary = "Get user by email",
      description = "Retrieves a user based on their email address.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/email/{email}")
  public ResponseEntity<AuthUserDto> getByEmail(
      @Parameter(description = "Email address of the user to be retrieved", required = true)
          @PathVariable("email")
          String email) {
    AuthUser user = this.identityProviderUseCases.findByEmail(email);
    return new ResponseEntity<>(authMapper.toUserDto(user), HttpStatus.OK);
  }

  /**
   * Retrieves a user by username.
   *
   * @param username the user username
   * @return the user
   */
  @Operation(
      summary = "Get user by username",
      description = "Retrieves a user based on their username.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/username/{username}")
  public ResponseEntity<AuthUserDto> getByUsername(
      @Parameter(description = "Username of the user to be retrieved", required = true)
          @PathVariable("username")
          String username) {
    AuthUser user = this.identityProviderUseCases.findByUsername(username).orElse(null);
    return new ResponseEntity<>(authMapper.toUserDto(user), HttpStatus.OK);
  }

  /**
   * Retrieves a user by custom attribute.
   *
   * @param query the custom attribute query
   * @return the user
   */
  @Operation(
      summary = "Get user by custom attribute",
      description =
          "Retrieves users based on a custom attribute query. Query format: key:value (e.g., phoneNumber:54750526 or email:test@mail.com)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = AuthUserDto.class)))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid query format",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/custom/{query}")
  public ResponseEntity<List<AuthUserDto>> getByCustomAttribute(
      @Parameter(
              description = "Custom attribute query in the format key:value",
              example = "phoneNumber:54750526",
              required = true)
          @PathVariable("query")
          String query) {
    List<AuthUser> user = this.identityProviderUseCases.findByCustomAttribute(query);
    return new ResponseEntity<>(authMapper.toUserDtoList(user), HttpStatus.OK);
  }

  /**
   * Retrieves all users with a specified role.
   *
   * @param roleType the role to filter by
   * @return the list of users
   */
  @Operation(
      summary = "Get users by role",
      description = "Retrieves all users with a specific role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = AuthUserDto.class)))),
        @ApiResponse(responseCode = "400", description = "Invalid role", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/role/{role}")
  public ResponseEntity<List<AuthUserDto>> getByRole(
      @Parameter(description = "Role to filter users by", required = true) @PathVariable("role")
          RoleType roleType) {
    List<AuthUser> users = this.identityProviderUseCases.findByRole(roleType);
    return new ResponseEntity<>(authMapper.toUserDtoList(users), HttpStatus.OK);
  }

  /**
   * Retrieves all users with a specified group.
   *
   * @param group the group to filter by
   * @return the list of users
   */
  @Operation(
      summary = "Get users by group",
      description = "Retrieves all users belonging to a specific group.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = AuthUserDto.class)))),
        @ApiResponse(responseCode = "400", description = "Invalid group", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/group/{group}")
  public ResponseEntity<List<AuthUserDto>> getByGroup(
      @Parameter(description = "Group to filter users by", required = true) @PathVariable("group")
          String group) {
    List<AuthUser> users = this.identityProviderUseCases.findByGroup(group);
    return new ResponseEntity<>(authMapper.toUserDtoList(users), HttpStatus.OK);
  }

  /**
   * Updates an existing user.
   *
   * @param userRequestDto the user request data
   * @return the updated user
   */
  @Operation(summary = "Update user", description = "Updates an existing user's information.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @PutMapping("/update")
  public ResponseEntity<AuthUserDto> update(@RequestBody @Valid UpdateUserRequest userRequestDto) {
    AuthUser authUser = authMapper.toAuthUser(userRequestDto);
    authUser = this.identityProviderUseCases.update(authUser);
    return new ResponseEntity<>(authMapper.toUserDto(authUser), HttpStatus.OK);
  }

  @Operation(
      summary = "Update user locale",
      description = "Updates the locale of the currently authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Locale updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid locale", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @PutMapping("/me/locale/{locale}")
  public ResponseEntity<AuthUserDto> updateLocale(
      @Parameter(description = "Locale to set for the user", required = true)
          @PathVariable("locale")
          Locale locale) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    AuthUser authUser =
        this.identityProviderUseCases.updateLocale(
            authentication.getName(), authentication.getAuthorities(), locale);
    return new ResponseEntity<>(authMapper.toUserDto(authUser), HttpStatus.OK);
  }

  /**
   * Retrieves all users with specified roles.
   *
   * @return the list of users
   */
  @Operation(
      summary = "Get all users (paginated)",
      description =
          "Retrieves all users with pagination and optional filtering by role and search.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomPage.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @GetMapping("/all")
  public ResponseEntity<CustomPage<AuthUserDto>> getAll(
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Search term") @RequestParam(defaultValue = "") String search,
      @Parameter(description = "Page number (starting from 0)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Sort field", example = "id")
          @RequestParam(defaultValue = "id", required = false)
          String sort,
      @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
          @RequestParam(defaultValue = "ASC")
          String sortDirection,
      @Parameter(description = "Filter by role") @RequestParam(required = false)
          RoleType roleType) {

    Pageable pageable = PaginationUtils.createPageable(page, size, sort, sortDirection);

    Page<AuthUser> users =
        this.identityProviderUseCases.findAll(List.of(String.valueOf(roleType)), search, pageable);
    CustomPage<AuthUserDto> userspage = PageMapper.toCustomPage(users.map(authMapper::toUserDto));
    return new ResponseEntity<>(userspage, HttpStatus.OK);
  }

  /**
   * Enables a user's active status.
   *
   * @param userId The ID of the user to enable.
   * @return The user with the enabled status.
   */
  @Operation(
      summary = "Enable user",
      description = "Enables a user account, allowing them to log in.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User enabled successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @PutMapping("/enable/{userId}")
  public ResponseEntity<AuthUserDto> enableUser(
      @Parameter(description = "ID of the user to enable", required = true) @PathVariable("userId")
          String userId) {
    AuthUser user = this.identityProviderUseCases.enableUser(userId);
    return new ResponseEntity<>(authMapper.toUserDto(user), HttpStatus.OK);
  }

  /**
   * Disables a user's active status.
   *
   * @param userId The ID of the user to disable.
   * @return The user with the disabled status.
   */
  @Operation(
      summary = "Disable user",
      description = "Disables a user account, preventing them from logging in.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User disabled successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @PutMapping("/disable/{userId}")
  public ResponseEntity<AuthUserDto> disableUser(
      @Parameter(description = "ID of the user to disable", required = true) @PathVariable("userId")
          String userId) {
    AuthUser user = this.identityProviderUseCases.disableUser(userId);
    return new ResponseEntity<>(authMapper.toUserDto(user), HttpStatus.OK);
  }

  /** toggle a user's active status. */
  @Operation(
      summary = "Toggle user status",
      description = "Toggles a user's active status between enabled and disabled.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User status toggled successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthUserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @PutMapping("/toggle/{userId}")
  public ResponseEntity<AuthUserDto> toggleUserStatus(
      @Parameter(description = "ID of the user to toggle status", required = true)
          @PathVariable("userId")
          String userId) {
    AuthUser user = this.identityProviderUseCases.toggleUserStatus(userId);
    return new ResponseEntity<>(authMapper.toUserDto(user), HttpStatus.OK);
  }

  /**
   * Deletes a user.
   *
   * @param userId the user ID
   */
  @Operation(summary = "Delete user", description = "Deletes a user permanently.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID of the user to delete", required = true) @PathVariable("userId")
          String userId) {
    identityProviderUseCases.deleteById(userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
