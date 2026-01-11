package com.example.test.repositories.notiRepo;

import com.example.test.models.entities.enums.NotificationStatus;
import com.example.test.models.entities.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByStatusAndSendAtBefore(NotificationStatus status, LocalDateTime dateTime);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.sendAt <= CURRENT_TIMESTAMP ORDER BY n.createdAt DESC")
    List<Notification> findVisibleNotifications(@Param("userId") Long userId);

}
