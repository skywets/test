package com.example.test.models.dtos.userDto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserHistoryDto {

    private Long userId;

    private String name;

    private String email;

    private boolean active;

    private LocalDateTime updateTime;
}
