package com.example.test.models.mappers.notificationMapper;

import com.example.test.models.dtos.notificationDto.CreateNotificationDto;
import com.example.test.models.dtos.notificationDto.NotificationDto;
import com.example.test.models.entities.notification.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    NotificationDto toDto(Notification notification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    Notification toEntity(CreateNotificationDto dto);
}
