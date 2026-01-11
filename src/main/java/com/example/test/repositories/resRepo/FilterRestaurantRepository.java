package com.example.test.repositories.resRepo;

import com.example.test.models.dtos.restaurantDto.RestaurantWithRatingProjection;
import com.example.test.models.entities.cuisine.CuisineType;
import com.example.test.models.entities.restaurant.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("""
                select 
                    r.id as id, 
                    r.name as name, 
                    r.address as address, 
                    r.open as open, 
                    r.avgCookingTimeMinutes as avgCookingTimeMinutes,
                    coalesce(avg(rv.grade), 0.0) as rating
                from Restaurant r
                left join r.menuItems mi
                left join Order o on o.restaurant = r
                left join Review rv on rv.order = o
                where (:cuisineType is null or :cuisineType in (select m.cuisineType from r.menuItems m))
                group by r.id, r.name, r.address, r.open, r.avgCookingTimeMinutes
                having (:minRating is null or coalesce(avg(rv.grade), 0.0) >= :minRating)
            """)
    Page<RestaurantWithRatingProjection> findAllWithFilter(
            @Param("cuisineType") CuisineType cuisineType,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

}