package com.example.test.models.entities.courier;

import com.example.test.models.entities.enums.CourierStatus;
import com.example.test.models.entities.enums.VehicleType;
import com.example.test.models.entities.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private boolean available = false;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    private CourierStatus status = CourierStatus.OFFLINE;
}
