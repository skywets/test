package com.example.test.services.notiService;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.notificationDto.CreateNotificationDto;
import com.example.test.models.dtos.notificationDto.NotificationDto;
import com.example.test.models.entities.enums.NotificationStatus;
import com.example.test.models.entities.notification.Notification;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.notificationMapper.NotificationMapper;
import com.example.test.repositories.notiRepo.NotificationRepository;
import com.example.test.repositories.userRepo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Override
    public NotificationDto create(CreateNotificationDto dto) {
        return notificationMapper.toDto(saveNotification(dto.getUserId(), dto.getMessage(), dto.getSendAt()));
    }

    public void createOrderNotification(Long userId, String message, LocalDateTime sendAt) {
        saveNotification(userId, message, sendAt);
    }

    private Notification saveNotification(Long userId, String message, LocalDateTime sendAt) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setStatus(NotificationStatus.PENDING);
        n.setSendAt(sendAt != null ? sendAt : LocalDateTime.now());
        return notificationRepository.save(n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getByUser(Long userId) {
        return notificationRepository.findVisibleNotifications(userId).stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    public void updateStatus(Long id, NotificationStatus status) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        n.setStatus(status);
        if (status == NotificationStatus.SENT)
            n.setSentAt(LocalDateTime.now());
        notificationRepository.save(n);
    }
}
