package com.example.test.unitTests;

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
import com.example.test.services.resService.impl.RestaurantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant Service Unit Tests")
class RestaurantServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private UserRepository userRepo;
    @Mock
    private RestaurantMapper restaurantMapper;
    @Mock
    private RestaurantApplicationRepository restaurantApplicationRepository;
    @Mock
    private MenuItemMapper menuItemMapper;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private User owner;
    private Restaurant restaurant;
    private RestaurantDto restaurantDto;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("owner@test.com").build();

        restaurant = new Restaurant();
        restaurant.setId(10L);
        restaurant.setName("Old Name");
        restaurant.setOwner(owner);

        restaurantDto = new RestaurantDto();
        restaurantDto.setName("New Restaurant");
    }

    @Test
    @DisplayName("Create Restaurant: Success when application is approved")
    void createRestaurant_Success() {
        RestaurantApplication app = new RestaurantApplication();
        app.setRestaurantCreated(false);

        given(restaurantApplicationRepository.findFirstByUserIdAndStatusAndRestaurantCreatedFalse(1L, Status.APPROVED))
                .willReturn(Optional.of(app));
        given(userRepo.findById(1L)).willReturn(Optional.of(owner));
        given(restaurantMapper.toEntity(any())).willReturn(restaurant);
        given(restaurantRepository.save(any())).willReturn(restaurant);
        given(restaurantMapper.toDto(any())).willReturn(new RestaurantDto());


        restaurantService.createRestaurant(1L, restaurantDto);


        assertThat(app.isRestaurantCreated()).isTrue();
        verify(restaurantRepository).save(restaurant);
        verify(restaurantApplicationRepository).save(app);
    }

    @Test
    @DisplayName("Update Profile: Success for owner")
    void updateProfile_Success() {
        RestaurantProfileUpdateDto updateDto = new RestaurantProfileUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setAddress("New Address");

        given(restaurantRepository.findByIdAndOwnerId(10L, 1L)).willReturn(Optional.of(restaurant));
        given(restaurantMapper.toDto(any())).willReturn(new RestaurantDto());


        restaurantService.updateProfile(10L, 1L, updateDto);


        assertThat(restaurant.getName()).isEqualTo("Updated Name");
        assertThat(restaurant.getAddress()).isEqualTo("New Address");
        verify(restaurantRepository).findByIdAndOwnerId(10L, 1L);
    }

    @Test
    @DisplayName("Update Profile: Throw AccessDenied if not the owner")
    void updateProfile_AccessDenied() {
        given(restaurantRepository.findByIdAndOwnerId(10L, 99L)).willReturn(Optional.empty());


        assertThatThrownBy(() -> restaurantService.updateProfile(10L, 99L, new RestaurantProfileUpdateDto()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Not your restaurant");
    }

    @Test
    @DisplayName("Get By ID: Should filter only available menu items")
    void getById_FiltersAvailableMenu() {
        MenuItem item1 = new MenuItem();
        item1.setAvailable(true);
        MenuItem item2 = new MenuItem();
        item2.setAvailable(false);
        restaurant.setMenuItems(Set.of(item1, item2));

        given(restaurantRepository.findById(10L)).willReturn(Optional.of(restaurant));


        RestaurantDetailsDto result = restaurantService.getById(10L);

        verify(menuItemMapper, times(1)).toDto(any());
    }

    @Test
    @DisplayName("Get My Restaurants: Success flow")
    void getMyRestaurants_Success() {
        given(restaurantRepository.findAllByOwnerId(1L)).willReturn(List.of(restaurant));
        given(restaurantMapper.toDto(any())).willReturn(new RestaurantDto());


        List<RestaurantDto> result = restaurantService.getMyRestaurants(1L);


        assertThat(result).hasSize(1);
        verify(restaurantRepository).findAllByOwnerId(1L);
    }

    @Test
    @DisplayName("Delete: Should call repository delete method")
    void deleteRestaurant_Success() {

        restaurantService.deleteRestaurant(10L);


        verify(restaurantRepository).deleteById(10L);
    }
}
