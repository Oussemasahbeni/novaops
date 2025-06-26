package com.novaops.userservice.infrastructure.dto.response;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import java.time.Instant;
import java.util.List;

public record AuthUserDto(
    String id,
    String email,
    String firstName,
    String lastName,
    String fullName,
    String username,
    List<RoleType> roleTypes,
    Locale locale,
    Boolean enabled,
    Boolean emailVerified,
    Instant createdAt) {}
