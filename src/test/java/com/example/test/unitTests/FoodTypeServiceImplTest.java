package com.example.test.unitTests;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.cuisine_foodTypeDto.FoodTypeDto;
import com.example.test.models.entities.cuisine_foodType.FoodType;
import com.example.test.models.mappers.cartMapper.FoodTypeMapper;
import com.example.test.repositories.foodTypeRepo.FoodTypeRepository;
import com.example.test.services.foodTypeService.impl.FoodTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FoodType Service Unit Tests")
class FoodTypeServiceImplTest {

    @Mock
    private FoodTypeRepository foodTypeRepository;
    @Mock
    private FoodTypeMapper foodTypeMapper;

    @InjectMocks
    private FoodTypeServiceImpl foodTypeService;

    private FoodType testEntity;
    private FoodTypeDto testDto;

    @BeforeEach
    void setUp() {
        testEntity = new FoodType();
        testEntity.setId(1L);
        testEntity.setName("Fast Food");

        testDto = new FoodTypeDto();
        testDto.setId(1L);
        testDto.setName("Fast Food");
    }

    @Test
    @DisplayName("Create FoodType: Success should save and return DTO")
    void create_Success() {
        when(foodTypeRepository.existsByName(testDto.getName())).thenReturn(false);
        when(foodTypeMapper.toEntity(testDto)).thenReturn(testEntity);
        when(foodTypeRepository.save(any(FoodType.class))).thenReturn(testEntity);
        when(foodTypeMapper.toDto(testEntity)).thenReturn(testDto);


        FoodTypeDto result = foodTypeService.create(testDto);


        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testDto.getName());
        verify(foodTypeRepository).save(any(FoodType.class));
    }

    @Test
    @DisplayName("Create FoodType: Throws exception when name already exists")
    void create_AlreadyExists_ThrowsException() {
        when(foodTypeRepository.existsByName(testDto.getName())).thenReturn(true);


        assertThatThrownBy(() -> foodTypeService.create(testDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(foodTypeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update FoodType: Success updates name and saves")
    void update_Success() {
        Long id = 1L;
        FoodTypeDto updateDto = new FoodTypeDto();
        updateDto.setName("Healthy Food");

        when(foodTypeRepository.findById(id)).thenReturn(Optional.of(testEntity));
        when(foodTypeRepository.save(any(FoodType.class))).thenReturn(testEntity);
        when(foodTypeMapper.toDto(testEntity)).thenReturn(updateDto);


        FoodTypeDto result = foodTypeService.update(id, updateDto);


        assertThat(testEntity.getName()).isEqualTo("Healthy Food");
        assertThat(result.getName()).isEqualTo("Healthy Food");
        verify(foodTypeRepository).save(testEntity);
    }

    @Test
    @DisplayName("Get By ID: Throws NotFoundException if missing")
    void getById_NotFound_ThrowsException() {
        when(foodTypeRepository.findById(99L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> foodTypeService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Get All: Success returns mapped list")
    void getAll_Success() {
        List<FoodType> entities = List.of(testEntity);
        List<FoodTypeDto> dtos = List.of(testDto);

        when(foodTypeRepository.findAll()).thenReturn(entities);
        when(foodTypeMapper.toDtoList(entities)).thenReturn(dtos);


        List<FoodTypeDto> result = foodTypeService.getAll();


        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fast Food");
        verify(foodTypeMapper).toDtoList(any());
    }

    @Test
    @DisplayName("Delete: Success when exists")
    void delete_Success() {
        when(foodTypeRepository.existsById(1L)).thenReturn(true);


        foodTypeService.delete(1L);


        verify(foodTypeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete: Throws NotFoundException when missing")
    void delete_NotFound_ThrowsException() {
        when(foodTypeRepository.existsById(1L)).thenReturn(false);


        assertThatThrownBy(() -> foodTypeService.delete(1L))
                .isInstanceOf(NotFoundException.class);

        verify(foodTypeRepository, never()).deleteById(any());
    }
}

