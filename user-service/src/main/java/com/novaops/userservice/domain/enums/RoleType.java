package com.novaops.userservice.domain.enums;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public enum RoleType {
  ADMIN,
  USER;

  public static Optional<RoleType> fromString(String roleStr) {
    for (RoleType roleType : RoleType.values()) {
      if (roleType.name().equalsIgnoreCase(roleStr)) {
        return Optional.of(roleType);
      }
    }
    return Optional.empty();
  }

  public static List<String> getAllRoleNames() {
    return Stream.of(RoleType.values()).map(Enum::name).toList();
  }
}
