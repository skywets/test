package com.example.test.unitTests;

import com.example.test.exceptions.InvalidCredentialsException;
import com.example.test.exceptions.UserAlreadyExistsException;
import com.example.test.models.dtos.userDto.LoginRequest;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import com.example.test.models.mappers.userMapper.UserHistoryMapper;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserHistoryRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.userService.impl.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserHistoryRepository historyRepository;
    @Mock
    private UserHistoryMapper historyMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserRegisterDto registerDto;
    private RoleT userRole;

    @BeforeEach
    void setUp() {
        registerDto = new UserRegisterDto();
        registerDto.setName("John Doe");
        registerDto.setEmail("john@example.com");
        registerDto.setPassword("plainPassword123");

        userRole = new RoleT();
        userRole.setId(1L);
        userRole.setName("USER");
    }

    @Test
    @DisplayName("Successful signup: should encode password and save user history")
    void signup_Success() {

        String encodedPassword = "encoded_hash_xyz";
        User savedUser = new User();
        savedUser.setId(100L);
        savedUser.setName(registerDto.getName());
        savedUser.setEmail(registerDto.getEmail());

        UserHistory history = new UserHistory();
        history.setUserId(100L);

        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerDto.getPassword())).thenReturn(encodedPassword);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(historyMapper.toEntity(any(User.class))).thenReturn(history);


        User result = authenticationService.signup(registerDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(registerDto.getEmail());

        verify(passwordEncoder).encode("plainPassword123");

        verify(roleRepository).findByName("USER");

        verify(historyRepository).save(any(UserHistory.class));
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals(encodedPassword) &&
                        user.getRoleTSet().contains(userRole)
        ));
    }

    @Test
    @DisplayName("Signup fails: should throw exception if email is already taken")
    void signup_EmailExists_ThrowsException() {
        when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);


        assertThatThrownBy(() -> authenticationService.signup(registerDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Successful authentication: should return user when credentials are valid")
    void authenticate_Success() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "password");
        User user = new User();
        user.setEmail("john@example.com");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        User result = authenticationService.authenticate(loginRequest);

        assertThat(result.getEmail()).isEqualTo(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Authentication fails: should throw InvalidCredentialsException on bad password")
    void authenticate_BadCredentials_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "wrong_password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));


        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Authentication fails: should throw exception if user not found after auth success")
    void authenticate_UserNotFoundAfterAuth_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("ghost@example.com", "password");
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());


        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}

