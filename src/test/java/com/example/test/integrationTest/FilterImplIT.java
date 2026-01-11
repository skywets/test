package com.example.test.integrationTest;

import com.example.test.models.dtos.restaurantDto.RestaurantFilter;
import com.example.test.models.dtos.restaurantDto.RestaurantListDto;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.cuisine.CuisineType;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.review.Review;
import com.example.test.models.entities.user.User;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.resRepo.FilterRestaurantRepository;
import com.example.test.repositories.reviewRepo.ReviewRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.resService.impl.FilterImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Restaurant Filter Integration Tests (ManyToMany)")
class FilterImplIT {

    @Autowired
    private FilterImpl filterService;
    @Autowired
    private FilterRestaurantRepository restaurantRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        restaurantRepository.deleteAll();
        menuItemRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .email("test@test.com")
                .password("pass")
                .build());

        MenuItem pasta = createMenuItem("Pasta", CuisineType.ITALIAN);
        MenuItem sushi = createMenuItem("Sushi", CuisineType.ASIA);

        Restaurant rest1 = Restaurant.builder()
                .name("Italian Place")
                .owner(testUser)
                .open(true)
                .menuItems(new HashSet<>(Set.of(pasta)))
                .build();
        rest1 = restaurantRepository.save(rest1);
        saveReview(rest1, 5);

        Restaurant rest2 = Restaurant.builder()
                .name("Asian Place")
                .owner(testUser)
                .open(true)
                .menuItems(new HashSet<>(Set.of(sushi)))
                .build();
        rest2 = restaurantRepository.save(rest2);
        saveReview(rest2, 3);
    }

    private MenuItem createMenuItem(String name, CuisineType type) {
        return menuItemRepository.save(MenuItem.builder()
                .name(name)
                .cuisineType(type)
                .price(BigDecimal.TEN)
                .available(true)
                .quantity(10)
                .build());
    }

    private void saveReview(Restaurant r, double grade) {
        Order o = Order.builder()
                .user(testUser)
                .restaurant(r)
                .status(OrderStatus.DELIVERED)
                .totalPrice(BigDecimal.TEN)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        o = orderRepository.save(o);

        Review rv = new Review();
        rv.setOrder(o);
        rv.setRestaurant(r);
        rv.setUser(testUser);
        rv.setGrade(grade);

        reviewRepository.save(rv);
    }


    @Test
    @DisplayName("Filter by Cuisine: Should handle ManyToMany relationship")
    void filterByCuisine() {
        RestaurantFilter filter = new RestaurantFilter(CuisineType.ITALIAN, null);
        Page<RestaurantListDto> result = filterService.findAll(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Italian Place");
    }

    @Test
    @DisplayName("Filter by Rating: Should return high rated restaurants")
    void filterByRating() {
        RestaurantFilter filter = new RestaurantFilter(null, 4.0);
        Page<RestaurantListDto> result = filterService.findAll(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Italian Place");
        assertThat(result.getContent().get(0).getRating()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Filter None: Should return all when filters are null")
    void filterNone() {
        RestaurantFilter filter = new RestaurantFilter(null, null);
        Page<RestaurantListDto> result = filterService.findAll(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }
}
