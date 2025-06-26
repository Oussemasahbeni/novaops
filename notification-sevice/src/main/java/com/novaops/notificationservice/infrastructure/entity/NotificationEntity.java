package com.novaops.notificationservice.infrastructure.entity;

import static jakarta.persistence.EnumType.STRING;
import static org.hibernate.type.SqlTypes.JSON;

import com.novaops.notificationservice.domain.enums.NotificationType;
import com.novaops.notificationservice.shared.AbstractAuditingEntity;
import jakarta.persistence.*;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.cache.annotation.Cacheable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(
    name = "notifications",
    indexes = {
      @Index(name = "idx_user_id_on_notifications", columnList = "userId"),
      @Index(name = "idx_is_read", columnList = "is_read")
    })
@Cacheable("notifications")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NotificationEntity extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Enumerated(STRING)
  @Column(name = "type", nullable = false)
  private NotificationType type;

  @JdbcTypeCode(JSON)
  @Column(name = "data", columnDefinition = "jsonb")
  private Map<String, Object> data;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead;
}
