package com.example.test.controllers.paymentController;

import com.example.test.models.dtos.payment.CreatePaymentDto;
import com.example.test.models.dtos.payment.PaymentDto;
import com.example.test.models.dtos.payment.UpdatePaymentStatusDto;
import com.example.test.services.paymentService.PaymentService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Operations related to processing and tracking payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Initiate a new payment", description = "Creates a payment record for a specific order.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment data provided")
    })
    @PostMapping
    public ResponseEntity<PaymentDto> create(
            @RequestBody @Valid CreatePaymentDto dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(dto, getUserId(auth)));
    }

    @Operation(summary = "Get payment details by ID", description = "Retrieves payment information if the user is the owner or admin.")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getById(
            @Parameter(description = "Payment ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(id, getUserId(auth)));
    }

    @Operation(summary = "Get payment by Order ID", description = "Retrieves payment details associated with a specific order.")
    @GetMapping(params = "orderId")
    public ResponseEntity<PaymentDto> getByOrderId(
            @Parameter(description = "Order ID") @RequestParam @NotNull @Positive Long orderId,
            @Parameter(hidden = true) Authentication auth
    ) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId, getUserId(auth)));
    }

    @Operation(summary = "Update payment status [ADMIN ONLY]", description = "Allows administrators to manually change the payment status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDto> updateStatus(
            @Parameter(description = "Payment ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody UpdatePaymentStatusDto dto
    ) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, dto.getStatus()));
    }

    private Long getUserId(Authentication auth) {
        return ((UserDetailsImpl) auth.getPrincipal()).getUser().getId();
    }
}
