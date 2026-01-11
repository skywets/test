package com.example.test.unitTests;

import com.example.test.exceptions.ApplicationAlreadyExistsException;
import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.restaurantDto.RestaurantApplicationDto;
import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.restaurant.RestaurantApplication;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.resMapper.RestaurantApplicationMapper;
import com.example.test.repositories.resRepo.RestaurantApplicationRepository;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.resService.impl.RestaurantApplicationServiceImpl;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant Application Service Unit Tests")
class RestaurantApplicationServiceImplTest {

    @Mock
    private RestaurantApplicationRepository restaurantApplicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RestaurantApplicationMapper mapper;

    @InjectMocks
    private RestaurantApplicationServiceImpl applicationService;

    private User testUser;
    private User testAdmin;
    private RestaurantApplication testApp;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .roleTSet(new HashSet<>())
                .build();

        testAdmin = User.builder()
                .id(99L)
                .build();

        testApp = RestaurantApplication.builder()
                .id(10L)
                .user(testUser)
                .status(Status.PENDING)
                .build();
    }

    @Test
    @DisplayName("Create: Success flow")
    void createApplication_Success() {
        RestaurantApplicationDto dto = new RestaurantApplicationDto();
        dto.setUserId(1L);
        dto.setDocuments("docs.pdf");

        given(restaurantApplicationRepository.existsByUserIdAndStatus(1L, Status.PENDING)).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(restaurantApplicationRepository.save(any())).willReturn(testApp);
        given(mapper.toDto(any())).willReturn(new RestaurantApplicationDto());


        applicationService.createApplication(dto);


        verify(restaurantApplicationRepository).save(argThat(app -> {
            assertThat(app.getStatus()).isEqualTo(Status.PENDING);
            assertThat(app.getUser().getId()).isEqualTo(1L);
            return true;
        }));
    }

    @Test
    @DisplayName("Create: Throw exception if pending application exists")
    void createApplication_AlreadyExists_ThrowsException() {
        RestaurantApplicationDto dto = new RestaurantApplicationDto();
        dto.setUserId(1L);
        given(restaurantApplicationRepository.existsByUserIdAndStatus(1L, Status.PENDING)).willReturn(true);


        assertThatThrownBy(() -> applicationService.createApplication(dto))
                .isInstanceOf(ApplicationAlreadyExistsException.class)
                .hasMessageContaining("You already have a pending application");
    }

    @Test
    @DisplayName("Approve: Should set APPROVED status and grant role")
    void approve_Success() {
        RoleT ownerRole = new RoleT();
        ownerRole.setName("RESTAURANT_OWNER");

        given(restaurantApplicationRepository.findById(10L)).willReturn(Optional.of(testApp));
        given(userRepository.findById(99L)).willReturn(Optional.of(testAdmin));
        given(roleRepository.findByName("RESTAURANT_OWNER")).willReturn(Optional.of(ownerRole));


        applicationService.approve(10L, 99L);


        assertThat(testApp.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(testApp.getProcessedByAdmin()).isEqualTo(testAdmin);
        assertThat(testUser.getRoleTSet()).contains(ownerRole);

        verify(userRepository).save(testUser);
        verify(restaurantApplicationRepository).save(testApp);
    }

    @Test
    @DisplayName("Approve: Throw exception if application already processed")
    void approve_AlreadyProcessed_ThrowsException() {
        testApp.setStatus(Status.REJECTED);
        given(restaurantApplicationRepository.findById(10L)).willReturn(Optional.of(testApp));


        assertThatThrownBy(() -> applicationService.approve(10L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Application already processed");
    }

    @Test
    @DisplayName("Reject: Success flow")
    void reject_Success() {
        given(restaurantApplicationRepository.findById(10L)).willReturn(Optional.of(testApp));
        given(userRepository.findById(99L)).willReturn(Optional.of(testAdmin));


        applicationService.reject(10L, 99L, "Bad documents");


        assertThat(testApp.getStatus()).isEqualTo(Status.REJECTED);
        assertThat(testApp.getAdminComment()).isEqualTo("Bad documents");
        assertThat(testApp.getProcessedByAdmin()).isEqualTo(testAdmin);

        verify(restaurantApplicationRepository).save(testApp);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get By User: Throw NotFound if list is empty")
    void getByUser_EmptyList_ThrowsException() {
        given(restaurantApplicationRepository.findAllByUserId(1L)).willReturn(List.of());


        assertThatThrownBy(() -> applicationService.getByUser(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("You have not submitted any restaurant applications yet");
    }
}

