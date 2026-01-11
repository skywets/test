package com.example.test.repositories.cartRepo;

import com.example.test.models.entities.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteAllByCartId(Long id);

}
