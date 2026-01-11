package com.example.test.models.entities.restaurant;

import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "restaurant_menuItems",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "menuItem_id")
    )
    private Set<MenuItem> menuItems;


    private String name;


    private String address;

    @Builder.Default
    private boolean open = true;


    private Integer avgCookingTimeMinutes;

    @ElementCollection
    @CollectionTable(
            name = "restaurant_prep_time",
            joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @Column(name = "prep_minutes")
    private final List<Integer> prepTimeHistoryMinutes = new ArrayList<>();

}
