package com.example.test.models.dtos.notificationDto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {

    private Long id;

    private Long userId;

    private String message;

    private String status;   // PENDING / SENT / READ

    private LocalDateTime sendAt;

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
}
