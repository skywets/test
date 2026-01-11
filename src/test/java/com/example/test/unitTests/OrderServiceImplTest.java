package com.example.test.unitTests;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.entities.cart.Cart;
import com.example.test.models.entities.cart.CartItem;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.orderMapper.OrderMapper;
import com.example.test.repositories.cartRepo.CartItemRepository;
import com.example.test.repositories.cartRepo.CartRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.resRepo.RestaurantRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.orderService.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Restaurant restaurant;
    private Cart cart;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        menuItem = new MenuItem();
        menuItem.setId(10L);
        menuItem.setName("Pizza");
        menuItem.setPrice(new BigDecimal("1000.00"));

        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Best Pizza");
        restaurant.setMenuItems(Set.of(menuItem));

        cart = new Cart();
        cart.setId(5L);
        CartItem cartItem = new CartItem();
        cartItem.setMenuItem(menuItem);
        cartItem.setQuantity(2);
        cart.setItems(new ArrayList<>(List.of(cartItem)));
    }

    @Test
    @DisplayName("Create Order: Success flow")
    void createOrderFromCart_Success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(restaurantRepository.findByIdWithMenuItems(1L)).willReturn(Optional.of(restaurant));
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));

        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));


        orderService.createOrderFromCart(1L, 1L, PaymentMethod.CASH);


        verify(orderRepository).save(argThat(order -> {
            assertThat(order.getTotalPrice()).isEqualByComparingTo("2000.00");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getOrderItems()).hasSize(1);
            return true;
        }));

        verify(cartItemRepository).deleteAllByCartId(5L);
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Create Order: Should throw exception if item not from this restaurant")
    void createOrderFromCart_WrongRestaurantItem() {
        MenuItem strangerItem = new MenuItem();
        strangerItem.setId(99L);

        CartItem badItem = new CartItem();
        badItem.setMenuItem(strangerItem);
        cart.setItems(List.of(badItem));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(restaurantRepository.findByIdWithMenuItems(1L)).willReturn(Optional.of(restaurant));
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));


        assertThatThrownBy(() -> orderService.createOrderFromCart(1L, 1L, PaymentMethod.CARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("не найдено в меню ресторана");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_UserNotFound_ThrowsException() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrderFromCart(1L, 1L, PaymentMethod.CASH))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void createOrder_RestaurantNotFound_ThrowsException() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(restaurantRepository.findByIdWithMenuItems(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrderFromCart(1L, 1L, PaymentMethod.CASH))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Restaurant not found");
    }

    @Test
    void createOrder_CartMissing_ThrowsException() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(restaurantRepository.findByIdWithMenuItems(1L)).willReturn(Optional.of(restaurant));
        given(cartRepository.findByUserId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrderFromCart(1L, 1L, PaymentMethod.CASH))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Cart is empty");
    }

    @Test
    void createOrder_CartItemsEmpty_ThrowsException() {
        cart.setItems(Collections.emptyList());

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(restaurantRepository.findByIdWithMenuItems(1L)).willReturn(Optional.of(restaurant));
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrderFromCart(1L, 1L, PaymentMethod.CASH))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot create order from empty cart");
    }

    @Test
    void createOrder_MultipleItems_CalculatesTotalCorrectny() {
        MenuItem item1 = new MenuItem();
        item1.setId(10L);
        item1.setPrice(new BigDecimal("100"));
        MenuItem item2 = new MenuItem();
        item2.setId(11L);
        item2.setPrice(new BigDecimal("250"));
        restaurant.setMenuItems(Set.of(item1, item2));

        CartItem ci1 = new CartItem();
        ci1.setMenuItem(item1);
        ci1.setQuantity(2);
        CartItem ci2 = new CartItem();
        ci2.setMenuItem(item2);
        ci2.setQuantity(3);
        cart.setItems(new ArrayList<>(List.of(ci1, ci2)));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(restaurantRepository.findByIdWithMenuItems(1L)).willReturn(Optional.of(restaurant));
        given(cartRepository.findByUserId(1L)).willReturn(Optional.of(cart));
        given(orderRepository.save(any())).willAnswer(i -> i.getArgument(0));

        orderService.createOrderFromCart(1L, 1L, PaymentMethod.CASH);

        verify(orderRepository).save(argThat(order -> {
            assertThat(order.getTotalPrice()).isEqualByComparingTo("950.00");
            return true;
        }));
        assertThat(cart.getItems()).isEmpty();

    }

    @Test
    @DisplayName("Get By ID: Success")
    void getById_Success() {
        Order order = new Order();
        order.setId(1L);
        OrderDto dto = new OrderDto();
        dto.setId(1L);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderMapper.toDto(order)).willReturn(dto);


        OrderDto result = orderService.getById(1L);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Get By ID: Throw NotFoundException")
    void getById_NotFound() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());


        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    @DisplayName("Get User Orders: Success with list")
    void getUserOrders_Success() {
        Long userId = 1L;
        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orders = List.of(order1, order2);

        given(orderRepository.findByUserId(userId)).willReturn(orders);
        given(orderMapper.toDto(any(Order.class))).willReturn(new OrderDto());


        List<OrderDto> result = orderService.getUserOrders(userId);


        assertThat(result).hasSize(2);
        verify(orderRepository).findByUserId(userId);
        verify(orderMapper, times(2)).toDto(any(Order.class));
    }

    @Test
    @DisplayName("Get User Orders: Return empty list when no orders")
    void getUserOrders_Empty() {
        given(orderRepository.findByUserId(1L)).willReturn(List.of());


        List<OrderDto> result = orderService.getUserOrders(1L);


        assertThat(result).isEmpty();
        verify(orderMapper, never()).toDto(any());
    }


}

