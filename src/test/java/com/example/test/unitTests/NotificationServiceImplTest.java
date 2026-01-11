package com.example.test.unitTests;

import com.example.test.models.dtos.notificationDto.CreateNotificationDto;
import com.example.test.models.dtos.notificationDto.NotificationDto;
import com.example.test.models.entities.enums.NotificationStatus;
import com.example.test.models.entities.notification.Notification;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.notificationMapper.NotificationMapper;
import com.example.test.repositories.notiRepo.NotificationRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.notiService.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Unit Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");

        testNotification = new Notification();
        testNotification.setId(100L);
        testNotification.setUser(testUser);
        testNotification.setMessage("Test Message");
        testNotification.setStatus(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("Create Notification: Should use provided sendAt time")
    void create_WithSpecificSendAt_Success() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        CreateNotificationDto dto = new CreateNotificationDto(1L, "Future Message", futureTime);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));


        notificationService.create(dto);


        verify(notificationRepository).save(argThat(n ->
                n.getSendAt().equals(futureTime) &&
                        n.getStatus() == NotificationStatus.PENDING
        ));
    }

    @Test
    @DisplayName("Create Notification: Should use current time if sendAt is null")
    void create_WithNullSendAt_UsesCurrentTime() {
        CreateNotificationDto dto = new CreateNotificationDto(1L, "Instant Message", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));


        notificationService.create(dto);


        verify(notificationRepository).save(argThat(n ->
                n.getSendAt() != null &&
                        n.getSendAt().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
    }

    @Test
    @DisplayName("Update Status: Should set sentAt when status is SENT")
    void updateStatus_ToSent_SetsSentAt() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(testNotification));


        notificationService.updateStatus(100L, NotificationStatus.SENT);


        assertThat(testNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(testNotification.getSentAt()).isNotNull();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("Update Status: Should not set sentAt when status is READ")
    void updateStatus_ToRead_DoesNotSetSentAt() {
        testNotification.setStatus(NotificationStatus.SENT);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(testNotification));


        notificationService.updateStatus(100L, NotificationStatus.READ);


        assertThat(testNotification.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(testNotification.getSentAt()).isNull();
    }

    @Test
    @DisplayName("Get By User: Should map only visible notifications")
    void getByUser_Success() {
        when(notificationRepository.findVisibleNotifications(1L)).thenReturn(List.of(testNotification));
        when(notificationMapper.toDto(any())).thenReturn(new NotificationDto());


        List<NotificationDto> result = notificationService.getByUser(1L);


        assertThat(result).hasSize(1);
        verify(notificationRepository).findVisibleNotifications(1L);
    }

    @Test
    @DisplayName("Create Order Notification: Internal call should save entity")
    void createOrderNotification_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));


        notificationService.createOrderNotification(1L, "Order update", null);


        verify(notificationRepository).save(any(Notification.class));
        verify(userRepository).findById(1L);
    }
}

