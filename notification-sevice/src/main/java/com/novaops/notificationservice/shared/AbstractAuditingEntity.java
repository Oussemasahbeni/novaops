package com.novaops.notificationservice.shared;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
public abstract class AbstractAuditingEntity {

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "last_modified_by")
  private String lastModifiedBy;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  private Short version;
}
