package com.example.test.controllers.user;

import com.example.test.models.dtos.userDto.UpdatePasswordDto;
import com.example.test.models.dtos.userDto.UpdateProfileDto;
import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.services.userService.UserProfileService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Profile", description = "Endpoints for managing the currently authenticated user's profile and security settings")
public class ProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "Get current user profile")
    @GetMapping
    public UserDto getProfile(Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        return userProfileService.getCurrentProfile(principal.getUsername());
    }

    @Operation(summary = "Update profile information")
    @PutMapping
    public ResponseEntity<Map<String, String>> updateProfile(
            @Valid @RequestBody UpdateProfileDto dto,
            Authentication auth
    ) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        userProfileService.updateProfile(principal.getUser().getId(), dto);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @Operation(summary = "Update user password")
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @Valid @RequestBody UpdatePasswordDto dto,
            Authentication auth
    ) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        userProfileService.updatePassword(principal.getUser().getId(), dto);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}