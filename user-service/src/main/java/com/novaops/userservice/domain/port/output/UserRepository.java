package com.novaops.userservice.domain.port.output;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.DomainBlob;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port interface for User domain operations. Defines the contract for user persistence and
 * retrieval operations in the hexagonal architecture.
 */
public interface UserRepository {

    /**
     * Finds a user by their unique identifier.
     *
     * @param id the unique identifier of the user
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findById(UUID id);

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Deletes a user by their unique identifier.
     *
     * @param id the unique identifier of the user to delete
     */
    void deleteById(UUID id);

    /**
     * Updates a user with the provided update request data.
     *
     * @param updateUserRequest the request containing updated user data
     * @return the updated user
     */
    User update(UpdateUserRequest updateUserRequest);

    /**
     * Updates a user entity.
     *
     * @param user the user entity to update
     * @return the updated user
     */
    User update(User user);

    /**
     * Updates the locale preference for a user.
     *
     * @param id     the user identifier
     * @param locale the new locale preference
     */
    void updateLocale(String id, Locale locale);

    /**
     * Creates a new user with the specified roles.
     *
     * @param user the user entity to create
     * @return the created user
     */
    User create(User user);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check
     * @return true if a user exists with the email, false otherwise
     */
    Boolean existsByEmail(String email);

    /**
     * Retrieves all users with pagination and filtering options.
     *
     * @param search   the search term for filtering users
     * @param pageable the pagination information
     * @param role     the role type to filter by
     * @return a page of users matching the criteria
     */
    Page<User> findAll(String search, Pageable pageable, RoleType role);

    DomainBlob uploadProfilePicture(MultipartFile file);
}
