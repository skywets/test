package com.example.test.models.mappers.review;

import com.example.test.models.dtos.review.ReviewDto;
import com.example.test.models.entities.review.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.restaurant.id", target = "restaurantId")
    ReviewDto toDto(Review review);
}
