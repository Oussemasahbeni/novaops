package com.novaops.userservice.domain.port.input;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.model.UserRegistrationData;
import com.novaops.userservice.domain.model.csv.CsvImportOptions;
import com.novaops.userservice.domain.model.csv.CsvValidationResult;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/** Interface defining the use cases for managing users. */
public interface UserUseCases {

  /**
   * Finds a user by their unique identifier.
   *
   * @param id the unique identifier of the user
   * @return the user associated with the given id
   */
  User findById(UUID id);

  User findCurrentUser(UUID id);

  User createUser(UserRegistrationData userRequestDto);

  CsvValidationResult importUsers(MultipartFile file, CsvImportOptions options) throws IOException;

  Boolean existsByEmail(String email);

  Page<User> findAllPaginated(String search, RoleType role, Pageable pageable);

  User updateUser(UUID id, UpdateUserRequest updatedProfileRequest);

  void deleteById(UUID id);
}
