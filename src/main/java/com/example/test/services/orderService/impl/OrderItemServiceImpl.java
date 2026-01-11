package com.example.test.services.orderService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.orderDto.OrderItemDto;
import com.example.test.models.mappers.orderMapper.OrderItemMapper;
import com.example.test.repositories.orderRepo.OrderItemRepository;
import com.example.test.services.orderService.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository repository;
    private final OrderItemMapper mapper;

    @Override
    public OrderItemDto getById(Long id) {
        return mapper.toDto(
                repository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Order item not found"))
        );
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
