package com.novaops.userservice.infrastructure.entity;

import java.util.UUID;

/** Projection for {@link UserEntity} */
public interface UserEntityForNotification {

    UUID getId();

    String getFirstName();

    String getLastName();

    String getEmail();
}
