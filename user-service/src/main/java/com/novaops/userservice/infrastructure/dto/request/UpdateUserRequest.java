package com.novaops.userservice.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUserRequest(
        @NotNull(message = "User ID is required") UUID id,
        @NotNull(message = "First name is required")
        @NotBlank(message = "First name is required")
        @Size(min = 2, message = "First name must be at least 2 characters long")
        String firstName,
        @NotNull(message = "Last name is required")
        @NotBlank(message = "Last name is required")
        @Size(min = 2, message = "Last name must be at least 2 characters long")
        String lastName,
        @NotNull(message = "Phone number is required")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "\\d{8}", message = "Phone number must be exactly 8 digits")
        String phoneNumber,
        String address) {
}