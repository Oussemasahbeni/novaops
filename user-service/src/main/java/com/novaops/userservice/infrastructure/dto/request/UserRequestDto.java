package com.novaops.userservice.infrastructure.dto.request;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.shared.validators.NotEmptyCollection;
import jakarta.validation.constraints.*;
import java.util.List;

public record UserRequestDto(
    @NotBlank(message = "First name is required")
        @Size(min = 2, message = "First name must be at least 2 characters long")
        String firstName,
    @NotBlank(message = "Last name is required")
        @Size(min = 2, message = "Last name must be at least 2 characters long")
        String lastName,
    @Email(message = "Invalid email address") @NotBlank(message = "Email is required") String email,
    @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "\\d{8}", message = "Phone number must be exactly 8 digits")
        String phoneNumber,
    String address,
    @NotEmptyCollection(message = "Roles are required") List<RoleType> roles) {}
