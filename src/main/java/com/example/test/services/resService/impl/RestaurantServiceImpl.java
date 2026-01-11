package com.example.test.services.resService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.restaurantDto.RestaurantDetailsDto;
import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.models.dtos.restaurantDto.RestaurantProfileUpdateDto;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.restaurant.RestaurantApplication;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.cartMapper.MenuItemMapper;
import com.example.test.models.mappers.resMapper.RestaurantMapper;
import com.example.test.repositories.resRepo.RestaurantApplicationRepository;
import com.example.test.repositories.resRepo.RestaurantRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.resService.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepo;
    private final RestaurantMapper restaurantMapper;
    private final RestaurantApplicationRepository restaurantApplicationRepository;
    private final MenuItemMapper menuItemMapper;


    @Transactional
    public RestaurantDto createRestaurant(Long ownerId, RestaurantDto dto) {

        RestaurantApplication app =
                restaurantApplicationRepository
                        .findFirstByUserIdAndStatusAndRestaurantCreatedFalse(
                                ownerId, Status.APPROVED
                        )
                        .orElseThrow(() ->
                                new RuntimeException("No approved application available")
                        );

        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Restaurant restaurant = restaurantMapper.toEntity(dto);
        restaurant.setOwner(owner);

        restaurant = restaurantRepository.save(restaurant);

        app.setRestaurantCreated(true);
        restaurantApplicationRepository.save(app);

        return restaurantMapper.toDto(restaurant);
    }


    @Override
    @Transactional
    public RestaurantDto updateProfile(
            Long restaurantId,
            Long ownerId,
            RestaurantProfileUpdateDto dto
    ) {
        Restaurant restaurant = getOwnedRestaurant(restaurantId, ownerId);

        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());

        return restaurantMapper.toDto(restaurant);
    }


    @Override
    @Transactional
    public RestaurantDto updateOpenStatus(
            Long restaurantId,
            boolean open,
            Long ownerId
    ) {
        Restaurant restaurant = getOwnedRestaurant(restaurantId, ownerId);

        restaurant.setOpen(open);

        return restaurantMapper.toDto(restaurant);
    }


    @Override
    @Transactional(readOnly = true)
    public List<RestaurantDto> getMyRestaurants(Long ownerId) {
        return restaurantRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(restaurantMapper::toDto)
                .toList();
    }

    private Restaurant getOwnedRestaurant(Long restaurantId, Long ownerId) {
        return restaurantRepository
                .findByIdAndOwnerId(restaurantId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Not your restaurant"));
    }


    @Override
    @Transactional(readOnly = true)
    public RestaurantDetailsDto getById(Long restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        RestaurantDetailsDto dto = new RestaurantDetailsDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setOpen(restaurant.isOpen());
        dto.setAvgCookingTimeMinutes(restaurant.getAvgCookingTimeMinutes());

        dto.setMenu(
                restaurant.getMenuItems()
                        .stream()
                        .filter(MenuItem::isAvailable)
                        .map(menuItemMapper::toDto)
                        .toList()
        );

        return dto;
    }

    @Override
    public List<RestaurantDto> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(restaurantMapper::toDto)
                .toList();
    }


    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }
}