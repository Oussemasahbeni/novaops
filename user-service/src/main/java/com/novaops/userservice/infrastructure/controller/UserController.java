package com.novaops.userservice.infrastructure.controller;

import static com.novaops.userservice.shared.AuthUtils.getCurrentAuthenticatedUserId;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.model.csv.CsvImportOptions;
import com.novaops.userservice.domain.model.csv.CsvValidationResult;
import com.novaops.userservice.domain.port.input.UserUseCases;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import com.novaops.userservice.infrastructure.dto.request.UserRequestDto;
import com.novaops.userservice.infrastructure.dto.response.UserDto;
import com.novaops.userservice.infrastructure.mapper.UserMapper;
import com.novaops.userservice.shared.pagination.CustomPage;
import com.novaops.userservice.shared.pagination.PageMapper;
import com.novaops.userservice.shared.pagination.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Tag(
    name = "Users",
    description =
        "Operations related to managing users, including retrieving user information, updating user preferences, and updating user profile.")
@RequiredArgsConstructor
@Slf4j
public class UserController {
  private final UserUseCases usersUseCases;
  private final UserMapper userMapper;

  @Operation(summary = "Create user", description = "Creates a new normal user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @PostMapping
  @PreAuthorize("hasRole('ROLE_admin')")
  public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
    User authUser = this.usersUseCases.createUser(userMapper.fromRequestDto(userRequestDto));
    return new ResponseEntity<>(userMapper.toUserDto(authUser), HttpStatus.CREATED);
  }

  @GetMapping("{id}")
  @Operation(
      summary = "Find user by ID",
      description = "Retrieve user information by their unique ID.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  public ResponseEntity<UserDto> findById(
      @Parameter(description = "ID of the user to be retrieved", required = true) @PathVariable
          UUID id) {
    return ResponseEntity.ok(userMapper.toUserDto(usersUseCases.findById(id)));
  }

  @GetMapping("/me")
  @Operation(
      summary = "Get current authenticated user",
      description = "Retrieve information for the currently authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful retrieval",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  public ResponseEntity<UserDto> findMe() {
    UUID id = getCurrentAuthenticatedUserId();
    User user = usersUseCases.findById(id);
    UserDto userDto = userMapper.toUserDto(user);
    return ResponseEntity.ok(userDto);
  }

  @Operation(
      summary = "Find all users",
      description =
          "Retrieve all users with pagination, sorting, and filtering options. Supports sorting by fields and filtering based on criteria.")
  @GetMapping()
  public ResponseEntity<CustomPage<UserDto>> findAll(
      @RequestParam(defaultValue = "0", required = false) int page,
      @RequestParam(defaultValue = "10", required = false) int size,
      @RequestParam(defaultValue = "id", required = false) String sort,
      @RequestParam(defaultValue = "", required = false) String search,
      @RequestParam(defaultValue = "DESC") String sortDirection,
      @RequestParam(required = false) RoleType role) {
    Pageable pageable = PaginationUtils.createPageable(page, size, sort, sortDirection);

    Page<UserDto> usersPage =
        usersUseCases.findAllPaginated(search, role, pageable).map(userMapper::toUserDto);
    return ResponseEntity.ok(PageMapper.toCustomPage(usersPage));
  }

  @GetMapping("/email-exists")
  public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
    return ResponseEntity.ok(usersUseCases.existsByEmail(email));
  }

  @Operation(
      summary = "Update user profile",
      description =
          "Update profile details such as name, email, etc., for the current authenticated user.")
  @PutMapping()
  public ResponseEntity<UserDto> updateUser(
      @Valid @RequestBody UpdateUserRequest updatedProfileRequest) {
    UUID id = getCurrentAuthenticatedUserId();
    User user = usersUseCases.updateUser(id, updatedProfileRequest);
    return new ResponseEntity<>(userMapper.toUserDto(user), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user", description = "Delete a user by their unique ID.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User deleted successfully",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content)
      })
  @PreAuthorize("hasRole('ROLE_admin')")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "ID of the user to be deleted", required = true) @PathVariable
          UUID id) {
    usersUseCases.deleteById(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/validate-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CsvValidationResult> validateCsv(
      @RequestParam("file") MultipartFile file, @RequestPart("options") CsvImportOptions options) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    try {
      CsvValidationResult result = usersUseCases.importUsers(file, options);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Failed to validate CSV file", e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
