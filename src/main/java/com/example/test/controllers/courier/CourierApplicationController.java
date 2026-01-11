package com.example.test.controllers.courier;

import com.example.test.models.dtos.courierDto.CourierApplicationDto;
import com.example.test.services.courierService.CourierApplicationService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/applications/couriers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Courier Application", description = "Endpoints for users to apply as couriers and track their applications")
public class CourierApplicationController {

    private final CourierApplicationService courierApplicationService;

    @Operation(summary = "Submit a new courier application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application submitted successfully",
                    content = @Content(schema = @Schema(implementation = CourierApplicationDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or application already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Authentication required")
    })
    @PostMapping
    public ResponseEntity<CourierApplicationDto> create(
            @Valid @RequestBody CourierApplicationDto dto,
            Authentication auth
    ) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        Long userId = principal.getUser().getId();

        log.info("User ID {} is creating a courier application", userId);

        dto.setUserId(userId);
        CourierApplicationDto createdApplication = courierApplicationService.createApplication(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdApplication);
    }

    @Operation(summary = "Get current user's courier applications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of applications retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No applications found for this user")
    })
    @GetMapping("/me")
    public ResponseEntity<List<CourierApplicationDto>> myApplications(Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        Long userId = principal.getUser().getId();

        List<CourierApplicationDto> applications = courierApplicationService.getByUser(userId);

        return ResponseEntity.ok(applications);
    }
}
