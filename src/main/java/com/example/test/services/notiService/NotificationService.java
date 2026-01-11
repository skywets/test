package com.example.test.services.notiService;

import com.example.test.models.dtos.notificationDto.CreateNotificationDto;
import com.example.test.models.dtos.notificationDto.NotificationDto;
import com.example.test.models.entities.enums.NotificationStatus;

import java.util.List;

public interface NotificationService {

    NotificationDto create(CreateNotificationDto dto);

    List<NotificationDto> getByUser(Long userId);

    void updateStatus(Long notificationId, NotificationStatus status);
}
