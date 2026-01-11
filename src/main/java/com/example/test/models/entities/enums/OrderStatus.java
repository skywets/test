package com.example.test.models.entities.enums;

public enum OrderStatus {

    CREATED,
    CONFIRMED,
    COOKED,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED;


    public boolean isActiveForCourier() {
        return this == CREATED
                || this == CONFIRMED
                || this == COOKED
                || this == IN_DELIVERY;
    }

    }