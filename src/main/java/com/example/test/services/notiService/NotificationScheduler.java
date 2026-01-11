package com.example.test.services.notiService;

import com.example.test.models.entities.enums.NotificationStatus;
import com.example.test.models.entities.notification.Notification;
import com.example.test.repositories.notiRepo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 60_000)
    public void sendScheduledNotifications() {

        List<Notification> notifications =
                notificationRepository.findAllByStatusAndSendAtBefore(
                        NotificationStatus.PENDING,
                        LocalDateTime.now()
                );

        for (Notification notification : notifications) {
            try {
                log.info("Sending notification id={}", notification.getMessage());

                notificationService.updateStatus(
                        notification.getId(),
                        NotificationStatus.SENT
                );

            } catch (Exception e) {
                log.error("Failed to send notification {}", notification.getId(), e);
            }
        }
    }
}
