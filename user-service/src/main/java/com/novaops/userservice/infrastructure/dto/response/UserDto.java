package com.novaops.userservice.infrastructure.dto.response;

import com.novaops.userservice.domain.enums.Gender;
import com.novaops.userservice.domain.enums.NotificationPreference;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String fullName,
    String firstName,
    String lastName,
    String profilePicture,
    String phoneNumber,
    String address,
    LocalDate birthDate,
    Gender gender,
    NotificationPreference notificationPreference,
    Instant createdAt,
    Instant updatedAt) {}
