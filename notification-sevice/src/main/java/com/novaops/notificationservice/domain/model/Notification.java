package com.novaops.notificationservice.domain.model;

import com.novaops.notificationservice.domain.enums.NotificationType;
import com.novaops.notificationservice.shared.AbstractAuditingModel;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Notification extends AbstractAuditingModel {
  private UUID id;
  private NotificationType type;
  private Map<String, Object> data;
  private UUID userId;
  private Boolean isRead;
}
