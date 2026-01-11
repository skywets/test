package com.example.test.unitTests;

import com.example.test.exceptions.ConflictException;
import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.dtos.userDto.UserFilter;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import com.example.test.models.mappers.userMapper.UserHistoryMapper;
import com.example.test.models.mappers.userMapper.UserMapper;
import com.example.test.models.mappers.userMapper.UserRegisterMapper;
import com.example.test.repositories.userRepo.UserHistoryRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.userService.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRegisterMapper userRegisterMapper;
    @Mock
    private UserHistoryRepository historyRepository;
    @Mock
    private UserHistoryMapper historyMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserHistory testHistory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .active(true)
                .build();

        testHistory = new UserHistory();
    }

    @Test
    @DisplayName("Create: Success flow - creates user and history record")
    void create_Success() {
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("test@test.com")
                .password("password")
                .build();

        given(userRepository.existsByEmail(dto.getEmail())).willReturn(false);
        given(userRegisterMapper.toModel(dto)).willReturn(testUser);
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(historyMapper.toEntity(any(User.class))).willReturn(testHistory);


        userService.create(dto);


        verify(userRepository).save(testUser);
        verify(historyRepository).save(argThat(history -> {
            assertThat(history.getUserId()).isEqualTo(1L);
            return true;
        }));
    }

    @Test
    @DisplayName("Create: Should throw ConflictException if email exists")
    void create_EmailExists_ThrowsConflict() {
        UserRegisterDto dto = UserRegisterDto.builder().email("exists@test.com").build();
        given(userRepository.existsByEmail("exists@test.com")).willReturn(true);


        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("FindById: Success flow")
    void findById_Success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userMapper.toDTO(testUser)).willReturn(new UserDto());


        UserDto result = userService.findById(1L);


        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("FindById: Should throw IllegalArgumentException for invalid ID")
    void findById_InvalidId_ThrowsException() {

        assertThatThrownBy(() -> userService.findById(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid user id");
    }

    @Test
    @DisplayName("Deactivate: Success flow - updates status and adds history")
    void deactivate_Success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(historyMapper.toEntity(any(User.class))).willReturn(testHistory);


        userService.deactivate(1L);


        assertThat(testUser.isActive()).isFalse();
        verify(userRepository).save(testUser);
        verify(historyRepository).save(testHistory);
        assertThat(testHistory.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("FindAll: Should call repository with Specification")
    void findAllByFilter_CallsRepository() {
        Pageable pageable = mock(Pageable.class);
        given(userRepository.findAll(any(Specification.class), eq(pageable)))
                .willReturn(Page.empty());


        userService.findAllByFilter(new UserFilter(null), pageable);


        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }
}

