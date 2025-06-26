package com.novaops.userservice.infrastructure.dto.request;

import com.novaops.userservice.shared.validators.NotEmptyCollection;
import java.util.List;

public record findAllIdpUserRequest(
    @NotEmptyCollection List<String> roles, String search, int page, int size) {}
