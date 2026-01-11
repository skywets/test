package com.example.test.repositories.foodTypeRepo;

import com.example.test.models.entities.cuisine_foodType.FoodType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodTypeRepository extends JpaRepository<FoodType, Long> {

    boolean existsByName(String name);
}
