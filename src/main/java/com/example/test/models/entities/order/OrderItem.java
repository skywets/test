package com.example.test.models.entities.order;

import com.example.test.models.entities.cart.MenuItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "orderItem_menuItem",
            joinColumns = @JoinColumn(name = "orderItem_id"),
            inverseJoinColumns = @JoinColumn(name = "menuItem_id")
    )
    private MenuItem menuItem;

    private int quantity;

    private BigDecimal price;
}
