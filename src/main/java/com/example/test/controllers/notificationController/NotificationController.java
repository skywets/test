package com.example.test.controllers.notificationController;

import com.example.test.models.dtos.notificationDto.CreateNotificationDto;
import com.example.test.models.dtos.notificationDto.NotificationDto;
import com.example.test.models.dtos.notificationDto.UpdateNotificationStatusDto;
import com.example.test.models.entities.enums.NotificationStatus;
import com.example.test.services.notiService.NotificationService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Operations for system and user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Create a new notification [ADMIN]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created successfully",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only ADMIN can create notifications")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> create(@Valid @RequestBody CreateNotificationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(dto));
    }

    @Operation(summary = "Get current user's notifications [AUTHENTICATED]")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        List<NotificationDto> notifications = notificationService.getByUser(principal.getUser().getId());
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Update notification status (SENT/READ) [ADMIN/OWNER]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status value provided"),
            @ApiResponse(responseCode = "403", description = "Access denied: Cannot update someone else's notification")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @notificationSecurity.isOwner(#id, authentication)")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody UpdateNotificationStatusDto dto,
            Authentication authentication
    ) {

        NotificationStatus newStatus;
        try {
            newStatus = NotificationStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid notification status");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && newStatus != NotificationStatus.READ) {
            throw new AccessDeniedException("Users are only allowed to mark notifications as READ");
        }

        notificationService.updateStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("message", "Notification status updated to %s".formatted(newStatus)));
    }
}
