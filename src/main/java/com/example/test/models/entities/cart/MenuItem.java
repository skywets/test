package com.example.test.models.entities.cart;

import com.example.test.models.entities.cuisine.CuisineType;
import com.example.test.models.entities.cuisine_foodType.FoodType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private CuisineType cuisineType;

    @ManyToMany
    @JoinTable(
            name = "foodTypes",
            joinColumns = @JoinColumn(name = "menuItem_id"),
            inverseJoinColumns = @JoinColumn(name = "foodType_id")
    )
    private List<FoodType> foodTypes;
}
