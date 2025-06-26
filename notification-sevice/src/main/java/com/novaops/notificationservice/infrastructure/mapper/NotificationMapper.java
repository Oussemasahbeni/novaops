package com.novaops.notificationservice.infrastructure.mapper;

import com.novaops.notificationservice.domain.model.Notification;
import com.novaops.notificationservice.infrastructure.dto.response.NotificationDto;
import com.novaops.notificationservice.infrastructure.entity.NotificationEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {
  public abstract NotificationEntity toEntity(Notification notification);

  public abstract Notification toNotification(NotificationEntity notificationEntity);

  public abstract List<NotificationEntity> toEntities(List<Notification> notifications);

  public abstract List<Notification> toNotifications(List<NotificationEntity> notificationEntities);

  public abstract List<NotificationDto> toDtos(List<Notification> notifications);

  public abstract NotificationDto toDto(Notification notification);
}
