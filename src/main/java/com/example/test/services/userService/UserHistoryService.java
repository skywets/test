package com.example.test.services.userService;

import com.example.test.models.dtos.userDto.UserHistoryDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface UserHistoryService {

    UserHistoryDto getUserStateAt(Long userId, LocalDate date);

    void saveHistory(Long id);
}
