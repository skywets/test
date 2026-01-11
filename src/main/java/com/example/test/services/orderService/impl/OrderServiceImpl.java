package com.example.test.services.orderService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.dtos.orderDto.OrderFilter;
import com.example.test.models.entities.cart.Cart;
import com.example.test.models.entities.cart.CartItem;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.models.entities.enums.PaymentStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.order.OrderItem;
import com.example.test.models.entities.payment.Payment;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.orderMapper.OrderMapper;
import com.example.test.repositories.cartRepo.CartItemRepository;
import com.example.test.repositories.cartRepo.CartRepository;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.paymentRepo.PaymentRepository;
import com.example.test.repositories.resRepo.RestaurantRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.notiService.NotificationServiceImpl;
import com.example.test.services.orderService.OrderService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderMapper orderMapper;
    private final MenuItemRepository menuItemRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationServiceImpl notificationService;

    @Override
    @Transactional
    public OrderDto createOrderFromCart(Long userId, Long restaurantId, PaymentMethod paymentMethod) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Restaurant restaurant = restaurantRepository.findByIdWithMenuItems(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentMethod(paymentMethod);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            MenuItem menuItem = cartItem.getMenuItem();

            boolean itemExistsInRestaurant = restaurant.getMenuItems().stream()
                    .anyMatch(mi -> mi.getId().equals(menuItem.getId()));

            if (!itemExistsInRestaurant) {
                throw new IllegalStateException("Блюдо '" + menuItem.getName() +
                        "' не найдено в меню ресторана " + restaurant.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal priceAtOrderTime = menuItem.getPrice();
            orderItem.setPrice(priceAtOrderTime);

            totalPrice = totalPrice.add(priceAtOrderTime.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItems.add(orderItem);
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.getItems().clear();

        return orderMapper.toDto(savedOrder);
    }


    @Override
    public OrderDto getById(Long id) {
        return orderMapper.toDto(
                orderRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Order not found"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> findAllByFilter(OrderFilter filter, Pageable pageable, Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        Long currentUserId = principal.getUser().getId();

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return orderRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (roles.contains("ROLE_ADMIN")) {

                if (filter.userId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
                }
            } else if (roles.contains("ROLE_RESTAURANT_OWNER")) {

                predicates.add(cb.equal(root.get("restaurant").get("owner").get("id"), currentUserId));
            } else if (roles.contains("ROLE_USER")) {
                predicates.add(cb.equal(root.get("user").get("id"), currentUserId));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable).map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderDto updateStatus(Authentication authentication, Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = principal.getUser().getId();

        Set<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.contains("ROLE_RESTAURANT_OWNER")) {
            if (newStatus == OrderStatus.CONFIRMED && order.getPaymentMethod() == PaymentMethod.CARD) {
                throw new IllegalStateException("Order with CARD payment method cannot be confirmed manually by the restaurant.");
            }
            validateRestaurantOwner(order, currentUserId, newStatus);
        } else if (roles.contains("ROLE_COURIER")) {

            if (newStatus != OrderStatus.DELIVERED) {
                throw new AccessDeniedException("Courier can only set DELIVERED status");
            }

            if (order.getCourier() == null || !order.getCourier().getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You are not the assigned courier for this order");
            }

            if (order.getPaymentMethod() == PaymentMethod.CASH) {
                Payment payment = paymentRepository.findByOrderId(order.getId())
                        .orElseThrow(() -> new NotFoundException("Payment record not found"));

                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);
            }
        } else if (!roles.contains("ROLE_ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            int minutes = (int) ChronoUnit.MINUTES.between(order.getCreatedAt(), LocalDateTime.now());
            order.setCookTime(minutes);
        }

        orderRepository.save(order);

        String message = String.format("Статус вашего заказа №%d изменился на: %s", order.getId(), newStatus);
        notificationService.createOrderNotification(order.getUser().getId(), message, LocalDateTime.now());

        if (newStatus == OrderStatus.DELIVERED) {
            String reviewMessage = "Как вам ваш заказ? Оцените ресторан!";
            notificationService.createOrderNotification(
                    order.getUser().getId(),
                    reviewMessage,
                    LocalDateTime.now().plusMinutes(30)
            );
        }

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public void cancel(Long orderId, Authentication auth) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        validateCancelPermissions(order, auth);

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order cannot be cancelled in its current status: " + order.getStatus());
        }

        for (OrderItem item : order.getOrderItems()) {
            MenuItem menuItem = item.getMenuItem();
            if (menuItem != null) {
                menuItem.setQuantity(menuItem.getQuantity() + item.getQuantity());
                menuItemRepository.save(menuItem);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void validateRestaurantOwner(Order order, Long currentUserId, OrderStatus newStatus) {

        if (!order.getRestaurant().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("This order does not belong to your restaurant");
        }

        Set<OrderStatus> allowedStatuses = EnumSet.of(
                OrderStatus.CONFIRMED,
                OrderStatus.COOKED,
                OrderStatus.IN_DELIVERY,
                OrderStatus.CANCELLED
        );

        if (!allowedStatuses.contains(newStatus)) {
            throw new AccessDeniedException("Restaurant cannot set status: " + newStatus);
        }
    }

    private void validateCancelPermissions(Order order, Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        Long currentUserId = principal.getUser().getId();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = order.getUser().getId().equals(currentUserId);
        boolean isRestaurant = order.getRestaurant().getOwner().getId().equals(currentUserId);

        if (!isAdmin && !isOwner && !isRestaurant) {
            throw new AccessDeniedException("You don't have permission to cancel this order");
        }
    }

}