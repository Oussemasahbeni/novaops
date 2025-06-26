package com.novaops.userservice.domain.model;

import com.novaops.userservice.domain.enums.RoleType;
import java.util.List;

public record UserRegistrationData(
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String address,
    List<RoleType> roles) {}
