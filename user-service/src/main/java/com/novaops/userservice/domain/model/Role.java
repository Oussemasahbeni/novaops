package com.novaops.userservice.domain.model;

import com.novaops.userservice.domain.enums.RoleType;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {

  private UUID id;
  private User user;
  private RoleType name;
}
