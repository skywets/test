package com.example.test.services.userService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.userDto.UserHistoryDto;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import com.example.test.models.mappers.userMapper.UserHistoryMapper;
import com.example.test.repositories.userRepo.UserHistoryRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.userService.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class UserHistoryServiceImpl implements UserHistoryService {

    private final UserRepository userRepository;
    private final UserHistoryRepository historyRepository;
    private final UserHistoryMapper historyMapper;


    @Override
    public UserHistoryDto getUserStateAt(Long userId, LocalDate date) {
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        UserHistory history = historyRepository
                .findFirstByUserIdAndUpdateTimeLessThanEqualOrderByUpdateTimeDesc(userId, endOfDay)
                .orElseThrow(() -> new NotFoundException(
                        String.format("No history found for User ID %d on or before %s", userId, date)
                ));

        return historyMapper.toDto(history);
    }

    public void saveHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        historyRepository.save(historyMapper.toEntity(user));
    }
}
