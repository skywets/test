package com.example.test.unitTests;

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
import com.example.test.services.cartService.Impl.CartServiceImpl;
import com.example.test.services.etaService.EtaCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Business Logic Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EtaCalculationService etaService;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private MenuItem testItem;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCart = new Cart();
        testCart.setId(10L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        testRestaurant = new Restaurant();
        testRestaurant.setId(100L);
        testRestaurant.setName("Test Pizza");
        testRestaurant.setOpen(true);

        testItem = new MenuItem();
        testItem.setId(50L);
        testItem.setName("Margherita");
        testItem.setPrice(new BigDecimal("10.00"));
        testItem.setQuantity(10);
        testItem.setAvailable(true);
    }

    @Test
    @DisplayName("Successfully add new item to cart and reduce stock")
    void addItem_Success_NewItem() {
        Long userId = 1L, menuItemId = 50L;
        Integer quantityToAdd = 2;

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(testItem));
        when(restaurantRepository.findByMenuItemId(menuItemId)).thenReturn(Optional.of(testRestaurant));
        when(etaService.calculateEtaMinutes(any())).thenReturn(30);

        cartService.addItem(userId, menuItemId, quantityToAdd);


        assertThat(testItem.getQuantity()).isEqualTo(8);

        assertThat(testCart.getItems()).hasSize(1);
        assertThat(testCart.getItems().get(0).getQuantity()).isEqualTo(quantityToAdd);

        verify(menuItemRepository).save(testItem);
        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartRepository).save(testCart);
    }

    @Test
    @DisplayName("Throw exception when restaurant is closed")
    void addItem_RestaurantClosed_ThrowsException() {
        testRestaurant.setOpen(false);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(menuItemRepository.findById(50L)).thenReturn(Optional.of(testItem));
        when(restaurantRepository.findByMenuItemId(50L)).thenReturn(Optional.of(testRestaurant));

        assertThatThrownBy(() -> cartService.addItem(1L, 50L, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Restaurant is currently closed");
    }

    @Test
    @DisplayName("Throw exception when stock is insufficient")
    void addItem_LowStock_ThrowsException() {
        testItem.setQuantity(1);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(menuItemRepository.findById(50L)).thenReturn(Optional.of(testItem));
        when(restaurantRepository.findByMenuItemId(50L)).thenReturn(Optional.of(testRestaurant));

        assertThatThrownBy(() -> cartService.addItem(1L, 50L, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock!");
    }

    @Test
    @DisplayName("Successfully clear cart and return items to stock")
    void clearCart_Success_ReturnsItemsToStock() {
        CartItem cartItem = new CartItem();
        cartItem.setMenuItem(testItem);
        cartItem.setQuantity(3);
        testCart.getItems().add(cartItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        cartService.clearCart(1L);

        assertThat(testItem.getQuantity()).isEqualTo(13);
        assertThat(testCart.getItems()).isEmpty();
        verify(cartItemRepository).deleteAll(anyList());
    }

    @Test
    @DisplayName("Update item quantity: Increase quantity and decrease stock")
    void updateItem_Increase_Success() {
        CartItem existingItem = new CartItem();
        existingItem.setId(200L);
        existingItem.setCart(testCart);
        existingItem.setMenuItem(testItem);
        existingItem.setQuantity(2);
        testCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(existingItem));
        when(restaurantRepository.findByMenuItemId(any())).thenReturn(Optional.of(testRestaurant));
        when(etaService.calculateEtaMinutes(any())).thenReturn(30);

        cartService.updateItem(1L, 200L, 5);

        assertThat(testItem.getQuantity()).isEqualTo(7);
        assertThat(existingItem.getQuantity()).isEqualTo(5);
        verify(menuItemRepository).save(testItem);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    @DisplayName("Update item quantity: Decrease quantity and return stock")
    void updateItem_Decrease_Success() {
        CartItem existingItem = new CartItem();
        existingItem.setId(200L);
        existingItem.setCart(testCart);
        existingItem.setMenuItem(testItem);
        existingItem.setQuantity(5);
        testCart.getItems().add(existingItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(existingItem));
        when(restaurantRepository.findByMenuItemId(any())).thenReturn(Optional.of(testRestaurant));

        cartService.updateItem(1L, 200L, 2);

        assertThat(testItem.getQuantity()).isEqualTo(13);
        assertThat(existingItem.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Security: Throw AccessDeniedException when updating item in someone else's cart")
    void updateItem_ForeignCart_ThrowsAccessDenied() {
        Cart otherCart = new Cart();
        otherCart.setId(999L);

        CartItem itemOfOtherUser = new CartItem();
        itemOfOtherUser.setCart(otherCart);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(itemOfOtherUser));

        assertThatThrownBy(() -> cartService.updateItem(1L, 200L, 5))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Item does not belong to your cart");
    }

    @Test
    @DisplayName("Remove item: Item is deleted and stock is fully returned")
    void removeItem_Success() {
        CartItem itemToRemove = new CartItem();
        itemToRemove.setCart(testCart);
        itemToRemove.setMenuItem(testItem);
        itemToRemove.setQuantity(4);
        testCart.getItems().add(itemToRemove);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(itemToRemove));

        cartService.removeItem(1L, 200L);


        assertThat(testItem.getQuantity()).isEqualTo(14);
        assertThat(testCart.getItems()).isEmpty();
        verify(cartItemRepository).delete(itemToRemove);
        verify(cartRepository).save(testCart);
    }

    @Test
    @DisplayName("Get current cart: Create new cart if not exists")
    void getCurrentCart_CreateNew_WhenNotFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.getCurrentCart(1L);

        verify(userRepository).findById(1L);
        verify(cartRepository).save(any(Cart.class));
    }

}

