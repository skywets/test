package com.example.test.integrationTest;

import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.dtos.orderDto.OrderFilter;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.user.User;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.resRepo.RestaurantRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.orderService.impl.OrderServiceImpl;
import com.example.test.services.userService.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Order Filtering Integration Tests (H2)")
class OrderServiceImplFilterIT {

    @Autowired
    private OrderServiceImpl orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    private User user1;
    private User user2;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        restaurantRepository.deleteAll();


        user1 = createAndSaveUser("user1@test.com", "Иван");
        user2 = createAndSaveUser("user2@test.com", "Владелец");

        restaurant = new Restaurant();
        restaurant.setName("H2 Pizza");
        restaurant.setOwner(user2);
        restaurant = restaurantRepository.save(restaurant);

        createOrder(user1, restaurant, OrderStatus.CREATED);
        createOrder(user2, restaurant, OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("ROLE_USER: Security check - can only see own orders")
    void findAllByFilter_UserRole_LimitToSelf() {
        Authentication auth = createAuth(user1, "ROLE_USER");
        OrderFilter spoofedFilter = new OrderFilter(user2.getId(), null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<OrderDto> result = orderService.findAllByFilter(spoofedFilter, pageable, auth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("ROLE_ADMIN: Can filter by any user")
    void findAllByFilter_AdminRole_Success() {
        Authentication auth = createAuth(user1, "ROLE_ADMIN");
        OrderFilter filter = new OrderFilter(user2.getId(), null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<OrderDto> result = orderService.findAllByFilter(filter, pageable, auth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("ROLE_RESTAURANT_OWNER: Sees only orders of his restaurant")
    void findAllByFilter_OwnerRole_Success() {
        Authentication auth = createAuth(user2, "ROLE_RESTAURANT_OWNER");
        OrderFilter filter = new OrderFilter(null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Page<OrderDto> result = orderService.findAllByFilter(filter, pageable, auth);

        assertThat(result.getContent()).hasSize(2);
    }


    private void createOrder(User user, Restaurant rest, OrderStatus status) {
        user.setPassword("encoded_password_here");
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(rest);
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal("100.00"));
        orderRepository.save(order);
    }

    private Authentication createAuth(User user, String role) {
        UserDetailsImpl principal = new UserDetailsImpl(user);
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
    }

    private User createAndSaveUser(String email, String name) {
        User user = User.builder()
                .name(name)
                .email(email)
                .password("safe_password_123")
                .active(true)
                .roleTSet(new HashSet<>())
                .build();

        return userRepository.save(user);
    }

}
