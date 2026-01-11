package com.example.test.services.etaService;

import com.example.test.models.entities.restaurant.Restaurant;

public interface RestaurantPrepTimeService {

    int calculateP80PrepTime(Restaurant restaurant);
}
