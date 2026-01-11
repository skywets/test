package com.example.test.models.entities.courier;

import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "courier_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourierApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String documents;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false)
    private Status status = Status.PENDING;

    private String adminComment;

    @ManyToOne
    @JoinColumn(name = "processed_by_admin_id")
    private User processedByAdmin;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

}
