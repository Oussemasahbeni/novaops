package com.novaops.userservice.infrastructure.adapter.messaging;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.infrastructure.entity.RoleEntity;
import com.novaops.userservice.infrastructure.entity.UserEntity;
import com.novaops.userservice.infrastructure.repository.UserJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUserEventConsumer {

  private final UserJpaRepository userJpaRepository;

  @KafkaListener(topics = "keycloak-events", groupId = "user-service-group")
  public void consume(KeycloakEvent event) {

    if (event.type().equals("REGISTER")) {

      RoleEntity roleEntity = RoleEntity.builder().name(RoleType.USER).build();
      UserEntity user =
          UserEntity.builder()
              .id(UUID.fromString(event.userId()))
              .email(event.details().email())
              .firstName(event.details().firstName())
              .lastName(event.details().lastName())
              .roles(List.of(roleEntity))
              .build();
      userJpaRepository.save(user);
    }
  }
}
