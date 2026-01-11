package com.example.test.unitTests;

import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.order.OrderItem;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.user.User;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.services.orderService.impl.OrderServiceImpl;
import com.example.test.services.userService.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Cancel Service Unit Tests")
class OrderCancelTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private Authentication auth;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private User customer;
    private User owner;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(1L).email("user@test.com").build();

        owner = User.builder().id(2L).email("owner@test.com").build();

        Restaurant restaurant = new Restaurant();
        restaurant.setId(50L);
        restaurant.setOwner(owner);

        menuItem = new MenuItem();
        menuItem.setId(10L);
        menuItem.setQuantity(5);

        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(2);

        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setUser(customer);
        testOrder.setRestaurant(restaurant);
        testOrder.setOrderItems(new ArrayList<>(List.of(orderItem)));
    }

    private void mockAuth(Long userId, String role) {
        User currentUser = User.builder().id(userId).build();
        UserDetailsImpl principal = mock(UserDetailsImpl.class);

        lenient().when(principal.getUser()).thenReturn(currentUser);
        lenient().when(auth.getPrincipal()).thenReturn(principal);
        lenient().doReturn(List.of(new SimpleGrantedAuthority(role)))
                .when(auth).getAuthorities();
    }

    @Test
    @DisplayName("Cancel: Success by Customer")
    void cancel_Success_ByCustomer() {
        mockAuth(1L, "ROLE_USER");
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));

        orderService.cancel(100L, auth);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(menuItem.getQuantity()).isEqualTo(7);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Cancel: Success by Restaurant Owner")
    void cancel_Success_ByRestaurantOwner() {
        mockAuth(2L, "ROLE_RESTAURANT_OWNER");
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));

        orderService.cancel(100L, auth);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Cancel: Success by Admin")
    void cancel_Success_ByAdmin() {
        mockAuth(999L, "ROLE_ADMIN");
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));

        orderService.cancel(100L, auth);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Cancel: Throws AccessDeniedException for stranger")
    void cancel_Stranger_ThrowsException() {
        mockAuth(555L, "ROLE_USER");
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancel(100L, auth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You don't have permission");
    }

    @Test
    @DisplayName("Cancel: Throws IllegalStateException when already CANCELLED")
    void cancel_AlreadyCancelled_ThrowsException() {
        mockAuth(1L, "ROLE_USER");
        testOrder.setStatus(OrderStatus.CANCELLED);
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancel(100L, auth))
                .isInstanceOf(IllegalStateException.class);
    }
}
