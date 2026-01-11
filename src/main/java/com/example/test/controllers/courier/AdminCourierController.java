package com.example.test.controllers.courier;

import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.services.courierService.CourierApplicationService;
import com.example.test.services.courierService.CourierAssignmentService;
import com.example.test.services.courierService.CourierService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Courier Management", description = "Endpoints for managing couriers, applications, and assignments")
public class AdminCourierController {

    private final CourierApplicationService courierApplicationService;
    private final CourierAssignmentService courierAssignmentService;
    private final CourierService courierService;

    @Operation(summary = "Approve courier application [ADMIN]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courier application approved successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, String>> approve(
            @PathVariable @NotNull @Positive Long id,
            Authentication auth
    ) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        courierApplicationService.approve(id, principal.getUser().getId());
        return ResponseEntity.ok(Map.of("message", "Courier application approved successfully"));
    }

    @Operation(summary = "Reject courier application [ADMIN]")
    @PostMapping("/reject/{id}")
    public ResponseEntity<Map<String, String>> reject(
            @PathVariable @NotNull @Positive Long id,
            @RequestBody @NotBlank String comment,
            Authentication auth
    ) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        courierApplicationService.reject(id, principal.getUser().getId(), comment);
        return ResponseEntity.ok(Map.of("message", "Courier application rejected successfully"));
    }

    @Operation(summary = "Assign order to courier [ADMIN]")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Map<String, String>> assignOrder(
            @PathVariable @NotNull @Positive Long id,
            @RequestParam @NotNull @Positive Long orderId
    ) {
        courierAssignmentService.assignOrderToCourier(id, orderId);
        return ResponseEntity.ok(Map.of("message", "Order assigned to courier successfully"));
    }

    @Operation(summary = "Get active orders for a specific courier [ADMIN/OWNER]")
    @GetMapping("/{id}/orders")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('COURIER') and @courierSecurity.isOwner(#id, authentication))")
    public List<OrderDto> getCourierOrders(@PathVariable @NotNull @Positive Long id) {
        return courierAssignmentService.getActiveOrders(id);
    }

    @Operation(summary = "Get courier details by ID [ADMIN]")
    @GetMapping("/{id}")
    public CourierDto get(@PathVariable @NotNull @Positive Long id) {
        return courierService.getCourier(id);
    }

    @Operation(summary = "Get all couriers [ADMIN]")
    @GetMapping
    public List<CourierDto> getAll() {
        return courierService.getAllCouriers();
    }

    @Operation(summary = "Delete courier [ADMIN]")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NotNull @Positive Long id) {
        courierService.deleteCourier(id);
        return ResponseEntity.ok(Map.of("message", "Courier deleted successfully"));
    }
}
