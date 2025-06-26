package com.novaops.notificationservice.infrastructure.adapter.persistence;

import com.novaops.notificationservice.infrastructure.entity.NotificationEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecification {

  private NotificationSpecification() {}

  public static Specification<NotificationEntity> hasUserId(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("userId"), userId);
  }
}
