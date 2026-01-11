package com.example.test.controllers.user;

import com.example.test.models.dtos.userDto.LoginRequest;
import com.example.test.models.dtos.userDto.LoginResponse;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtService;
import com.example.test.services.userService.impl.AuthenticationService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Authentication", description = "Endpoints for user registration and login")
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in the system and returns a success message.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
                    @ApiResponse(responseCode = "422", description = "Validation error")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegisterDto dto) {
        authenticationService.signup(dto);
        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully"
        ));
    }

    @Operation(
            summary = "Authenticate user",
            description = "Verifies user credentials and returns a JWT token with its expiration time.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid email or password"),
                    @ApiResponse(responseCode = "400", description = "Invalid request format")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        User user = authenticationService.authenticate(loginRequest);
        UserDetails userDetails = new UserDetailsImpl(user);
        String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(
                LoginResponse.builder()
                        .token(jwt)
                        .expiresIn(jwtService.getExpirationTime())
                        .build()
        );
    }
}
