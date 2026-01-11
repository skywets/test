package com.example.test.unitTests;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.cuisine_foodType.FoodType;
import com.example.test.models.mappers.cartMapper.MenuItemMapper;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.foodTypeRepo.FoodTypeRepository;
import com.example.test.services.cartService.Impl.MenuItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuItem Service Unit Tests")
class MenuItemServiceImplTest {

    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private FoodTypeRepository foodTypeRepository;
    @Mock
    private MenuItemMapper menuItemMapper;

    @InjectMocks
    private MenuItemServiceImpl menuItemService;

    private MenuItem testItem;
    private MenuItemDto testDto;
    private List<FoodType> foodTypes;

    @BeforeEach
    void setUp() {
        testItem = new MenuItem();
        testItem.setId(1L);
        testItem.setName("Pizza");
        testItem.setPrice(new BigDecimal("500.00"));
        testItem.setQuantity(10);
        testItem.setAvailable(true);

        testDto = new MenuItemDto();
        testDto.setName("Pizza");
        testDto.setPrice(new BigDecimal("500.00"));
        testDto.setFoodTypeIds(List.of(10L, 11L));

        FoodType ft1 = new FoodType();
        ft1.setId(10L);
        FoodType ft2 = new FoodType();
        ft2.setId(11L);
        foodTypes = List.of(ft1, ft2);
    }

    @Test
    @DisplayName("Create MenuItem with FoodTypes - Success")
    void create_WithFoodTypes_Success() {
        when(menuItemMapper.toEntity(any(MenuItemDto.class))).thenReturn(testItem);
        when(foodTypeRepository.findAllById(testDto.getFoodTypeIds())).thenReturn(foodTypes);
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(testItem);
        when(menuItemMapper.toDto(any(MenuItem.class))).thenReturn(testDto);


        MenuItemDto result = menuItemService.create(testDto);


        assertThat(result).isNotNull();
        assertThat(testItem.getFoodTypes()).hasSize(2);
        verify(foodTypeRepository).findAllById(testDto.getFoodTypeIds());
        verify(menuItemRepository).save(testItem);
    }

    @Test
    @DisplayName("Update existing MenuItem - Success")
    void update_ExistingItem_Success() {
        Long itemId = 1L;
        when(menuItemRepository.findById(itemId)).thenReturn(Optional.of(testItem));
        when(foodTypeRepository.findAllById(anyList())).thenReturn(foodTypes);
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));
        when(menuItemMapper.toDto(any(MenuItem.class))).thenReturn(testDto);


        MenuItemDto result = menuItemService.update(itemId, testDto);


        assertThat(result).isNotNull();
        verify(menuItemRepository).findById(itemId);
        verify(menuItemRepository).save(argThat(item ->
                item.getName().equals(testDto.getName()) &&
                        item.getFoodTypes().size() == 2
        ));
    }

    @Test
    @DisplayName("Update MenuItem - Should throw NotFoundException if not exists")
    void update_NotExists_ThrowsException() {
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> menuItemService.update(99L, testDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");

        verify(menuItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get MenuItem by ID - Success")
    void getById_Success() {
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(menuItemMapper.toDto(testItem)).thenReturn(testDto);


        MenuItemDto result = menuItemService.getById(1L);


        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pizza");
    }

    @Test
    @DisplayName("Delete MenuItem - Success")
    void delete_Success() {
        when(menuItemRepository.existsById(1L)).thenReturn(true);


        menuItemService.delete(1L);


        verify(menuItemRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete MenuItem - Throws Exception if not found")
    void delete_NotFound_ThrowsException() {
        when(menuItemRepository.existsById(1L)).thenReturn(false);


        assertThatThrownBy(() -> menuItemService.delete(1L))
                .isInstanceOf(NotFoundException.class);

        verify(menuItemRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Get all available items - Success")
    void getAvailable_Success() {
        when(menuItemRepository.findByAvailableTrue()).thenReturn(List.of(testItem));
        when(menuItemMapper.toDto(any())).thenReturn(testDto);


        List<MenuItemDto> result = menuItemService.getAvailable();


        assertThat(result).hasSize(1);
        verify(menuItemRepository).findByAvailableTrue();
    }
}

