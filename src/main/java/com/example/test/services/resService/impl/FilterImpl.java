package com.example.test.services.resService.impl;

import com.example.test.models.dtos.restaurantDto.RestaurantFilter;
import com.example.test.models.dtos.restaurantDto.RestaurantListDto;
import com.example.test.repositories.resRepo.FilterRestaurantRepository;
import com.example.test.services.resService.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilterImpl implements Filter {

    private final FilterRestaurantRepository restaurantRepository;

    @Override
    public Page<RestaurantListDto> findAll(
            RestaurantFilter filter,
            Pageable pageable
    ) {

        return restaurantRepository
                .findAllWithFilter(
                        filter.cuisineType(),
                        filter.minRating(),
                        pageable
                )
                .map(p -> new RestaurantListDto(
                        p.getId(),
                        p.getName(),
                        p.getAddress(),
                        p.isOpen(),
                        p.getAvgCookingTimeMinutes(),
                        p.getRating()
                ));
    }
}
