package com.example.test.unitTests;

import com.example.test.models.entities.enums.NotificationStatus;
import com.example.test.models.entities.notification.Notification;
import com.example.test.repositories.notiRepo.NotificationRepository;
import com.example.test.services.notiService.NotificationScheduler;
import com.example.test.services.notiService.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Scheduler Unit Tests")
class NotificationSchedulerTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @Test
    @DisplayName("Should process multiple pending notifications successfully")
    void sendScheduledNotifications_Success() {
        Notification n1 = new Notification();
        n1.setId(1L);
        n1.setMessage("Order Confirmed");

        Notification n2 = new Notification();
        n2.setId(2L);
        n2.setMessage("Order Delivered");

        when(notificationRepository.findAllByStatusAndSendAtBefore(
                eq(NotificationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(n1, n2));


        notificationScheduler.sendScheduledNotifications();

        verify(notificationService).updateStatus(1L, NotificationStatus.SENT);
        verify(notificationService).updateStatus(2L, NotificationStatus.SENT);
        verify(notificationRepository).findAllByStatusAndSendAtBefore(eq(NotificationStatus.PENDING), any());
    }

    @Test
    @DisplayName("Fault Tolerance: Should continue processing if one notification fails")
    void sendScheduledNotifications_ContinueOnException() {
        Notification n1 = new Notification();
        n1.setId(1L);
        Notification n2 = new Notification();
        n2.setId(2L);
        Notification n3 = new Notification();
        n3.setId(3L);

        when(notificationRepository.findAllByStatusAndSendAtBefore(any(), any()))
                .thenReturn(List.of(n1, n2, n3));

        doThrow(new RuntimeException("Gateway Timeout"))
                .when(notificationService).updateStatus(2L, NotificationStatus.SENT);


        notificationScheduler.sendScheduledNotifications();

        verify(notificationService).updateStatus(1L, NotificationStatus.SENT);
        verify(notificationService).updateStatus(2L, NotificationStatus.SENT);
        verify(notificationService).updateStatus(3L, NotificationStatus.SENT);

    }

    @Test
    @DisplayName("Should do nothing if no pending notifications found")
    void sendScheduledNotifications_EmptyList_NoAction() {
        when(notificationRepository.findAllByStatusAndSendAtBefore(any(), any()))
                .thenReturn(List.of());

        notificationScheduler.sendScheduledNotifications();

        verify(notificationService, never()).updateStatus(anyLong(), any());
    }
}

