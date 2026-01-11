package com.example.test.services.etaService.impl;

import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.repositories.resRepo.RestaurantRepository;
import com.example.test.services.etaService.RestaurantPrepTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantPrepTimeServiceImpl implements RestaurantPrepTimeService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public int calculateP80PrepTime(Restaurant restaurant) {

        Pageable topOneHundred = PageRequest.of(0, 100);

        List<Integer> history = restaurantRepository.prepTimeHistoryMinutes(restaurant.getId(), topOneHundred);

        if (history.isEmpty()) {
            return restaurant.getAvgCookingTimeMinutes();
        }


        List<Integer> sorted = new ArrayList<>(history);
        Collections.sort(sorted);
        int index = (int) Math.ceil(0.8 * sorted.size()) - 1;
        return sorted.get(Math.max(index, 0));
    }
}