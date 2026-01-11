package com.example.test.models.entities.cuisine_foodType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "foodType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

}
