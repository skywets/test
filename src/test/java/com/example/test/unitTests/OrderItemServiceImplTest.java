package com.example.test.unitTests;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.orderDto.OrderItemDto;
import com.example.test.models.entities.order.OrderItem;
import com.example.test.models.mappers.orderMapper.OrderItemMapper;
import com.example.test.repositories.orderRepo.OrderItemRepository;
import com.example.test.services.orderService.impl.OrderItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Item Service Unit Tests")
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItem testItem;
    private OrderItemDto testDto;

    @BeforeEach
    void setUp() {
        testItem = new OrderItem();
        testItem.setId(1L);
        testItem.setPrice(new BigDecimal("500.00"));
        testItem.setQuantity(2);

        testDto = new OrderItemDto();
        testDto.setId(1L);
        testDto.setPrice(new BigDecimal("500.00"));
        testDto.setQuantity(2);
    }

    @Test
    @DisplayName("Get By ID: Should return DTO when item exists")
    void getById_Success() {
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(orderItemMapper.toDto(testItem)).thenReturn(testDto);


        OrderItemDto result = orderItemService.getById(1L);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("500.00"));

        verify(orderItemRepository).findById(1L);
        verify(orderItemMapper).toDto(testItem);
    }

    @Test
    @DisplayName("Get By ID: Should throw NotFoundException when item missing")
    void getById_NotFound_ThrowsException() {
        when(orderItemRepository.findById(99L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> orderItemService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Order item not found");

        verify(orderItemMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Delete: Should call repository delete method")
    void delete_Success() {
        Long id = 1L;


        orderItemService.delete(id);


        verify(orderItemRepository).deleteById(id);
    }
}

