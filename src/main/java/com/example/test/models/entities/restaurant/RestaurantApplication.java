package com.example.test.models.entities.restaurant;

import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String documents;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    private String adminComment;

    @ManyToOne
    @JoinColumn(name = "processed_by_admin_id")
    private User processedByAdmin;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean restaurantCreated;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

}
