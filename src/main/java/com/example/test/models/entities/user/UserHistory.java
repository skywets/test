package com.example.test.models.entities.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_history")
@Getter
@Setter
public class UserHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String name;

    private String email;

    private boolean active;

    @CreationTimestamp
    private LocalDateTime updateTime;

}
