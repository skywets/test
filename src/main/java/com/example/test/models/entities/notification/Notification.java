package com.example.test.models.entities.notification;

import com.example.test.models.entities.enums.NotificationStatus;
import com.example.test.models.entities.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_status_send_at", columnList = "status, send_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;


    private LocalDateTime sendAt;


    private LocalDateTime sentAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
