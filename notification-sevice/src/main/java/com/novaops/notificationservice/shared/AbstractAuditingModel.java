package com.novaops.notificationservice.shared;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public abstract class AbstractAuditingModel {

  private String createdBy;
  private String lastModifiedBy;
  private Instant createdAt;
  private Instant updatedAt;
  private Short version;
}
