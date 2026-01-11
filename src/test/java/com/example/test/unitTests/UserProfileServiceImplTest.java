package com.example.test.unitTests;

import com.example.test.exceptions.InvalidCredentialsException;
import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.userDto.UpdatePasswordDto;
import com.example.test.models.dtos.userDto.UpdateProfileDto;
import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import com.example.test.models.mappers.userMapper.UserHistoryMapper;
import com.example.test.models.mappers.userMapper.UserMapper;
import com.example.test.repositories.userRepo.UserHistoryRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.userService.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Profile Service Unit Tests")
class UserProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserHistoryRepository historyRepository;
    @Mock
    private UserHistoryMapper historyMapper;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User testUser;
    private UserHistory testHistory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Old Name")
                .email("old@test.com")
                .password("encoded_old_password")
                .build();

        testHistory = new UserHistory();
    }

    @Test
    @DisplayName("Get Profile: Success flow")
    void getCurrentProfile_Success() {
        given(userRepository.findByEmail("old@test.com")).willReturn(Optional.of(testUser));
        given(userMapper.toDTO(testUser)).willReturn(new UserDto());


        UserDto result = userProfileService.getCurrentProfile("old@test.com");


        assertThat(result).isNotNull();
        verify(userRepository).findByEmail("old@test.com");
    }

    @Test
    @DisplayName("Update Profile: Should update fields and save to history")
    void updateProfile_Success() {
        UpdateProfileDto dto = UpdateProfileDto.builder()
                .name("New Name")
                .email("new@test.com")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(historyMapper.toEntity(any(User.class))).willReturn(testHistory);


        userProfileService.updateProfile(1L, dto);


        assertThat(testUser.getName()).isEqualTo("New Name");
        assertThat(testUser.getEmail()).isEqualTo("new@test.com");

        verify(userRepository).save(testUser);
        verify(historyRepository).save(argThat(h -> {
            assertThat(h.getUserId()).isEqualTo(1L);
            return true;
        }));
    }

    @Test
    @DisplayName("Update Password: Success flow with encoding")
    void updatePassword_Success() {
        UpdatePasswordDto dto = UpdatePasswordDto.builder()
                .oldPassword("old_raw")
                .newPassword("new_raw")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("old_raw", "encoded_old_password")).willReturn(true);
        given(passwordEncoder.encode("new_raw")).willReturn("encoded_new_password");
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(historyMapper.toEntity(any(User.class))).willReturn(testHistory);


        userProfileService.updatePassword(1L, dto);


        assertThat(testUser.getPassword()).isEqualTo("encoded_new_password");
        verify(passwordEncoder).encode("new_raw");
        verify(historyRepository).save(testHistory);
    }

    @Test
    @DisplayName("Update Password: Should throw exception if old password incorrect")
    void updatePassword_WrongOldPassword_ThrowsException() {
        UpdatePasswordDto dto = UpdatePasswordDto.builder()
                .oldPassword("wrong_old")
                .newPassword("new_raw")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrong_old", "encoded_old_password")).willReturn(false);


        assertThatThrownBy(() -> userProfileService.updatePassword(1L, dto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Old password incorrect");

        verify(userRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Common: Should throw NotFoundException if user missing")
    void getUser_NotFound_ThrowsException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());


        assertThatThrownBy(() -> userProfileService.updateProfile(99L, new UpdateProfileDto()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }
}

