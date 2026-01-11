package com.example.test.unitTests;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.courierDto.CourierApplicationDto;
import com.example.test.models.entities.courier.CourierApplication;
import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.courierMapper.CourierApplicationMapper;
import com.example.test.repositories.courierRepo.CourierApplicationRepository;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.courierService.impl.CourierApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Courier Application Service Unit Tests")
class CourierApplicationServiceImplTest {

    @Mock
    private CourierApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private CourierApplicationMapper applicationMapper;

    @InjectMocks
    private CourierApplicationServiceImpl applicationService;

    private User testUser;
    private CourierApplication testApp;
    private CourierApplicationDto testDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setRoleTSet(new HashSet<>());

        testApp = new CourierApplication();
        testApp.setId(10L);
        testApp.setUser(testUser);
        testApp.setStatus(Status.PENDING);

        testDto = new CourierApplicationDto();
        testDto.setUserId(1L);
        testDto.setDocuments("Passport info");
    }

    @Test
    @DisplayName("Create Application: Success creates pending application")
    void createApplication_Success() {
        when(courierRepository.existsByUserId(1L)).thenReturn(false);
        when(applicationRepository.existsByUserIdAndStatus(1L, Status.PENDING)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(applicationRepository.save(any(CourierApplication.class))).thenReturn(testApp);
        when(applicationMapper.toDto(any())).thenReturn(testDto);


        CourierApplicationDto result = applicationService.createApplication(testDto);


        assertThat(result).isNotNull();
        verify(applicationRepository).save(argThat(app -> app.getStatus() == Status.PENDING));
    }

    @Test
    @DisplayName("Create Application: Throws exception if user is already a courier")
    void createApplication_AlreadyCourier_ThrowsException() {
        when(courierRepository.existsByUserId(1L)).thenReturn(true);


        assertThatThrownBy(() -> applicationService.createApplication(testDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already a courier");
    }

    @Test
    @DisplayName("Approve Application: Success creates courier entity and assigns role")
    void approve_Success() {
        Long adminId = 99L;
        User admin = new User();
        admin.setId(adminId);
        RoleT courierRole = new RoleT();
        courierRole.setName("COURIER");

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(testApp));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(roleRepository.findByName("COURIER")).thenReturn(Optional.of(courierRole));


        applicationService.approve(10L, adminId);

        assertThat(testApp.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(testApp.getProcessedByAdmin()).isEqualTo(admin);
        verify(courierRepository).save(argThat(c -> c.getUser().equals(testUser)));

        assertThat(testUser.getRoleTSet()).contains(courierRole);

        verify(userRepository).save(testUser);

        verify(applicationRepository).save(testApp);
    }

    @Test
    @DisplayName("Approve Application: Throws exception if already processed")
    void approve_AlreadyProcessed_ThrowsException() {
        testApp.setStatus(Status.REJECTED);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(testApp));


        assertThatThrownBy(() -> applicationService.approve(10L, 99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been processed");
    }

    @Test
    @DisplayName("Reject Application: Success sets status and comment")
    void reject_Success() {
        Long adminId = 99L;
        User admin = new User();
        admin.setId(adminId);
        String comment = "Bad documents";

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(testApp));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));


        applicationService.reject(10L, adminId, comment);


        assertThat(testApp.getStatus()).isEqualTo(Status.REJECTED);
        assertThat(testApp.getAdminComment()).isEqualTo(comment);
        verify(applicationRepository).save(testApp);
    }

    @Test
    @DisplayName("Reject Application: Throws exception if comment is empty")
    void reject_EmptyComment_ThrowsException() {

        assertThatThrownBy(() -> applicationService.reject(10L, 99L, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Get By User: Throws NotFound if list is empty")
    void getByUser_Empty_ThrowsNotFound() {
        when(applicationRepository.findAllByUserId(1L)).thenReturn(List.of());


        assertThatThrownBy(() -> applicationService.getByUser(1L))
                .isInstanceOf(NotFoundException.class);
    }
}

