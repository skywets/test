package com.example.test.controllers.courier;

import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.dtos.courierDto.CourierStatusDto;
import com.example.test.models.dtos.courierDto.CourierVehicleDto;
import com.example.test.services.courierService.CourierService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/courier/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COURIER')")
@Tag(name = "Courier Profile", description = "Endpoints for couriers to manage their own profile, vehicle, and availability status")
public class CourierProfileController {

    private final CourierService courierService;

    @Operation(summary = "Update courier vehicle type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = CourierDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle data provided"),
            @ApiResponse(responseCode = "403", description = "Access denied: Courier role required")
    })
    @PatchMapping("/vehicle")
    public ResponseEntity<CourierDto> updateVehicle(
            @Valid @RequestBody CourierVehicleDto dto,
            Authentication auth
    ) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        CourierDto updatedCourier = courierService.updateVehicle(principal.getUser().getId(), dto.getVehicleType());
        return ResponseEntity.ok(updatedCourier);
    }

    @Operation(summary = "Update courier availability status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = CourierDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "403", description = "Access denied: Courier role required")
    })
    @PatchMapping("/status")
    public ResponseEntity<CourierDto> updateStatus(
            @Valid @RequestBody CourierStatusDto dto,
            Authentication auth
    ) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        CourierDto updatedCourier = courierService.updateStatus(principal.getUser().getId(), dto.getStatus());
        return ResponseEntity.ok(updatedCourier);
    }
}
