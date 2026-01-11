package com.example.test.unitTests;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.CourierStatus;
import com.example.test.models.entities.enums.VehicleType;
import com.example.test.models.mappers.courierMapper.CourierMapper;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.services.courierService.impl.CourierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Courier Service Unit Tests")
class CourierServiceImplTest {

    @Mock
    private CourierRepository courierRepository;
    @Mock
    private CourierMapper courierMapper;

    @InjectMocks
    private CourierServiceImpl courierService;

    private Courier testCourier;
    private CourierDto testDto;

    @BeforeEach
    void setUp() {
        testCourier = new Courier();
        testCourier.setId(10L);
        testCourier.setStatus(CourierStatus.OFFLINE);
        testCourier.setAvailable(false);
        testCourier.setVehicleType(VehicleType.BIKE);

        testDto = new CourierDto();
        testDto.setId(10L);
        testDto.setStatus(CourierStatus.OFFLINE);
    }

    @Test
    @DisplayName("Update Vehicle: Success updates type and returns DTO")
    void updateVehicle_Success() {
        Long userId = 1L;
        when(courierRepository.findByUserId(userId)).thenReturn(Optional.of(testCourier));
        when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);
        when(courierMapper.toDto(any(Courier.class))).thenReturn(testDto);

        CourierDto result = courierService.updateVehicle(userId, VehicleType.CAR);

        assertThat(testCourier.getVehicleType()).isEqualTo(VehicleType.CAR);
        verify(courierRepository).save(testCourier);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Update Status: Success switches to AVAILABLE and sets available flag to true")
    void updateStatus_ToAvailable_Success() {
        Long userId = 1L;
        when(courierRepository.findByUserId(userId)).thenReturn(Optional.of(testCourier));
        when(courierRepository.save(any(Courier.class))).thenReturn(testCourier);
        when(courierMapper.toDto(any())).thenReturn(testDto);

        courierService.updateStatus(userId, "AVAILABLE");

        assertThat(testCourier.getStatus()).isEqualTo(CourierStatus.AVAILABLE);
        assertThat(testCourier.isAvailable()).isTrue();
        verify(courierRepository).save(testCourier);
    }

    @Test
    @DisplayName("Update Status: Throws AccessDeniedException when setting restricted status (e.g. WORKING)")
    void updateStatus_RestrictedStatus_ThrowsAccessDeniedException() {
        Long userId = 1L;
        when(courierRepository.findByUserId(userId)).thenReturn(Optional.of(testCourier));

        assertThatThrownBy(() -> courierService.updateStatus(userId, "WORKING"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("manually can only switch to OFFLINE or AVAILABLE");

        verify(courierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Status: Throws IllegalArgumentException on invalid status string")
    void updateStatus_InvalidEnumString_ThrowsIllegalArgumentException() {
        Long userId = 1L;
        when(courierRepository.findByUserId(userId)).thenReturn(Optional.of(testCourier));


        assertThatThrownBy(() -> courierService.updateStatus(userId, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status value");
    }

    @Test
    @DisplayName("Get Courier: Success returns DTO")
    void getCourier_Success() {
        when(courierRepository.findById(10L)).thenReturn(Optional.of(testCourier));
        when(courierMapper.toDto(testCourier)).thenReturn(testDto);


        CourierDto result = courierService.getCourier(10L);


        assertThat(result).isNotNull();
        verify(courierRepository).findById(10L);
    }

    @Test
    @DisplayName("Delete Courier: Success when courier exists")
    void deleteCourier_Success() {
        when(courierRepository.existsById(10L)).thenReturn(true);

        courierService.deleteCourier(10L);

        verify(courierRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Delete Courier: Throws NotFoundException when courier missing")
    void deleteCourier_NotFound_ThrowsException() {
        when(courierRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> courierService.deleteCourier(10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Cannot delete");
    }
}

