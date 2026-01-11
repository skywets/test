package com.example.test.services.cartService.Impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.cartDto.CartDto;
import com.example.test.models.entities.cart.Cart;
import com.example.test.models.entities.cart.CartItem;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.cartMapper.CartMapper;
import com.example.test.repositories.cartRepo.CartItemRepository;
import com.example.test.repositories.cartRepo.CartRepository;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.resRepo.RestaurantRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.cartService.CartService;
import com.example.test.services.etaService.EtaCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final EtaCalculationService etaService;
    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartDto getCurrentCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createAndSaveNewCart(userId));
        return cartMapper.toDto(cart);
    }


    @Override
    public CartDto addItem(Long userId, Long menuItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

        Restaurant restaurant = restaurantRepository.findByMenuItemId(menuItemId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found for this item"));

        if (!restaurant.isOpen()) {
            throw new IllegalStateException("Restaurant is currently closed");
        }

        if (!menuItem.isAvailable()) {
            throw new IllegalStateException("Menu item is not available");
        }

        if (menuItem.getQuantity() < quantity) {
            throw new IllegalStateException("Not enough stock!");
        }

        menuItem.setQuantity(menuItem.getQuantity() - quantity);
        menuItemRepository.save(menuItem);

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItemId))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setMenuItem(menuItem);
                    newItem.setQuantity(0);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);

        recalculateEta(cart);
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Override
    public CartDto updateItem(Long userId, Long cartItemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getCart(userId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new AccessDeniedException("Item does not belong to your cart");
        }

        MenuItem menuItem = item.getMenuItem();
        int oldQuantity = item.getQuantity();
        int diff = newQuantity - oldQuantity;

        if (diff > 0) {
            if (menuItem.getQuantity() < diff) {
                throw new IllegalStateException("Not enough stock to increase quantity!");
            }
            menuItem.setQuantity(menuItem.getQuantity() - diff);
        } else if (diff < 0) {
            menuItem.setQuantity(menuItem.getQuantity() + Math.abs(diff));
        }

        item.setQuantity(newQuantity);
        menuItemRepository.save(menuItem);
        cartItemRepository.save(item);

        recalculateEta(cart);
        return cartMapper.toDto(cart);
    }

    @Override
    public void removeItem(Long userId, Long cartItemId) {
        Cart cart = getCart(userId);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        MenuItem menuItem = item.getMenuItem();
        menuItem.setQuantity(menuItem.getQuantity() + item.getQuantity());
        menuItemRepository.save(menuItem);

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        recalculateEta(cart);
        cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getCart(userId);

        for (CartItem item : cart.getItems()) {
            MenuItem menuItem = item.getMenuItem();
            menuItem.setQuantity(menuItem.getQuantity() + item.getQuantity());
            menuItemRepository.save(menuItem);
        }

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setDeliveryTime(null);
        cartRepository.save(cart);
    }

    private Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createAndSaveNewCart(userId));
    }

    private Cart createAndSaveNewCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private void recalculateEta(Cart cart) {
        if (cart.getItems().isEmpty()) {
            cart.setDeliveryTime(null);
            return;
        }

        Long firstMenuItemId = cart.getItems().get(0).getMenuItem().getId();
        Restaurant restaurant = restaurantRepository.findByMenuItemId(firstMenuItemId)
                .orElseThrow(() -> new IllegalStateException("Restaurant not found for menu item"));

        int etaMinutes = etaService.calculateEtaMinutes(restaurant);
        cart.setDeliveryTime(LocalDateTime.now().plusMinutes(etaMinutes));
    }
}
