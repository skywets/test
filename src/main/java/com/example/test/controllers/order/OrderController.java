package com.example.test.controllers.order;

import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.dtos.orderDto.OrderFilter;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.services.orderService.OrderService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management.")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create order from cart",
            description = "Processes and creates a new order for the authenticated user based on their active cart."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or empty cart"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @PostMapping
    public ResponseEntity<OrderDto> create(
            @Parameter(description = "ID of the restaurant", example = "1") @RequestParam @NotNull @Positive Long restaurantId,
            @Parameter(description = "Method of payment") @RequestParam @NotNull PaymentMethod paymentMethod,
            @Parameter(hidden = true) Authentication auth
    ) {
        OrderDto order = orderService.createOrderFromCart(getUserId(auth), restaurantId, paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves detailed information about a specific order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order details retrieved"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(
            @Parameter(description = "Internal ID of the order", example = "100") @PathVariable @NotNull @Positive Long id
    ) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(summary = "Get current user orders", description = "Returns a list of all orders belonging to the currently authenticated user.")
    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrders(@Parameter(hidden = true) Authentication auth) {
        return ResponseEntity.ok(orderService.getUserOrders(getUserId(auth)));
    }

    @Operation(
            summary = "Get all orders with filtering [ADMIN/STAFF]",
            description = "Allows administrators to retrieve a paginated list of orders filtered by user ID or status."
    )
    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAll(
            @Parameter(description = "Filter by User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by Order Status") @RequestParam(required = false) OrderStatus status,
            @ParameterObject Pageable pageable,
            @Parameter(hidden = true) Authentication authentication
    ) {
        OrderFilter filter = new OrderFilter(userId, status);
        return ResponseEntity.ok(orderService.findAllByFilter(filter, pageable, authentication));
    }

    @Operation(
            summary = "Update order status",
            description = "Updates the status of an existing order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: User does not have permission to set this status"),
            @ApiResponse(responseCode = "400", description = "Invalid status value provided"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @Parameter(description = "Internal ID of the order") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "New status for the order") @RequestParam @NotNull OrderStatus status,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(orderService.updateStatus(authentication, id, status));
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancels an order. Only accessible by the order owner or administration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Cannot cancel this order")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancel(
            @Parameter(description = "Internal ID of the order") @PathVariable @NotNull @Positive Long id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        orderService.cancel(id, authentication);
        return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
    }

    private Long getUserId(Authentication auth) {
        return ((UserDetailsImpl) auth.getPrincipal()).getUser().getId();
    }
}
