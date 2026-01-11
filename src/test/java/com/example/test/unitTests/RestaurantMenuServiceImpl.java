package com.example.test.unitTests;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.cartMapper.MenuItemMapper;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.foodTypeRepo.FoodTypeRepository;
import com.example.test.repositories.resRepo.FilterRestaurantRepository;
import com.example.test.services.resMenuService.impl.RestaurantMenuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant Menu Service Unit Tests")
class RestaurantMenuServiceImplTest {

    @Mock
    private FilterRestaurantRepository restaurantRepo;
    @Mock
    private MenuItemRepository menuItemRepo;
    @Mock
    private FoodTypeRepository foodTypeRepository;
    @Mock
    private MenuItemMapper mapper;

    @InjectMocks
    private RestaurantMenuServiceImpl menuService;

    private Restaurant testRestaurant;
    private User owner;
    private MenuItem testItem;
    private MenuItemDto testDto;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).build();

        testRestaurant = new Restaurant();
        testRestaurant.setId(10L);
        testRestaurant.setOwner(owner);
        testRestaurant.setMenuItems(new HashSet<>());

        testItem = new MenuItem();
        testItem.setId(100L);
        testItem.setName("Pizza");
        testItem.setPrice(new BigDecimal("500.00"));

        testDto = new MenuItemDto();
        testDto.setName("Pizza");
        testDto.setPrice(new BigDecimal("500.00"));
        testDto.setFoodTypeIds(List.of(1L, 2L));
    }

    @Test
    @DisplayName("Add Item: Success flow")
    void addMenuItem_Success() {
        given(restaurantRepo.findById(10L)).willReturn(Optional.of(testRestaurant));
        given(mapper.toEntity(testDto)).willReturn(testItem);
        given(foodTypeRepository.findAllById(any())).willReturn(List.of());
        given(menuItemRepo.save(any())).willReturn(testItem);
        given(mapper.toDto(any())).willReturn(testDto);


        MenuItemDto result = menuService.addMenuItem(10L, testDto, 1L);


        assertThat(result).isNotNull();
        assertThat(testRestaurant.getMenuItems()).contains(testItem);
        verify(menuItemRepo).save(testItem);
    }

    @Test
    @DisplayName("Security: Throw AccessDenied if not the owner")
    void getOwnedRestaurant_NotOwner_ThrowsException() {
        given(restaurantRepo.findById(10L)).willReturn(Optional.of(testRestaurant));

        assertThatThrownBy(() -> menuService.addMenuItem(10L, testDto, 99L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not owner of this restaurant");
    }

    @Test
    @DisplayName("Update Item: Success flow")
    void updateMenuItem_Success() {
        testRestaurant.getMenuItems().add(testItem);
        given(restaurantRepo.findById(10L)).willReturn(Optional.of(testRestaurant));
        given(menuItemRepo.findById(100L)).willReturn(Optional.of(testItem));
        given(menuItemRepo.save(any())).willReturn(testItem);


        menuService.updateMenuItem(10L, 100L, testDto, 1L);


        assertThat(testItem.getName()).isEqualTo("Pizza");
        verify(menuItemRepo).save(testItem);
    }

    @Test
    @DisplayName("Update Item: Throw exception if item not in restaurant's menu")
    void updateMenuItem_NotBelongToRestaurant_ThrowsException() {
        MenuItem strangerItem = new MenuItem();
        strangerItem.setId(999L);

        given(restaurantRepo.findById(10L)).willReturn(Optional.of(testRestaurant));
        given(menuItemRepo.findById(999L)).willReturn(Optional.of(strangerItem));


        assertThatThrownBy(() -> menuService.updateMenuItem(10L, 999L, testDto, 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Menu item does not belong to this restaurant");
    }

    @Test
    @DisplayName("Remove Item: Success flow")
    void removeMenuItem_Success() {
        testRestaurant.getMenuItems().add(testItem);
        given(restaurantRepo.findById(10L)).willReturn(Optional.of(testRestaurant));


        menuService.removeMenuItem(10L, 100L, 1L);


        assertThat(testRestaurant.getMenuItems()).isEmpty();
    }

    @Test
    @DisplayName("Remove Item: Throw NotFound if item not in list")
    void removeMenuItem_NotFoundInList_ThrowsException() {
        given(restaurantRepo.findById(10L)).willReturn(Optional.of(testRestaurant));

        assertThatThrownBy(() -> menuService.removeMenuItem(10L, 100L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Menu item not found in this restaurant");
    }

    @Test
    @DisplayName("Get Menu: Success flow")
    void getMenu_Success() {
        testRestaurant.getMenuItems().add(testItem);
        given(restaurantRepo.findById(10L)).willReturn(Optional.of(testRestaurant));
        given(mapper.toDto(any())).willReturn(testDto);


        List<MenuItemDto> menu = menuService.getMenu(10L);


        assertThat(menu).hasSize(1);
        verify(mapper, times(1)).toDto(any());
    }
}
