package com.example.test.repositories.resRepo;

import com.example.test.models.entities.restaurant.Restaurant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findAllByOwnerId(Long ownerId);

    Optional<Restaurant> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT o.cookTime FROM Order o WHERE o.restaurant.id = :restaurantId AND o.cookTime IS NOT NULL ORDER BY o.createdAt DESC")
    List<Integer> prepTimeHistoryMinutes(@Param("restaurantId") Long restaurantId, Pageable pageable);

    @Query("SELECT r FROM Restaurant r JOIN r.menuItems mi WHERE mi.id = :menuItemId")
    Optional<Restaurant> findByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.menuItems WHERE r.id = :id")
    Optional<Restaurant> findByIdWithMenuItems(@Param("id") Long id);


}


