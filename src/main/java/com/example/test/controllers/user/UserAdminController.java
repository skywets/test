package com.example.test.controllers.user;

import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.dtos.userDto.UserFilter;
import com.example.test.models.dtos.userDto.UserHistoryDto;
import com.example.test.services.userService.UserHistoryService;
import com.example.test.services.userService.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Administration", description = "Endpoints for managing users and viewing history. Access restricted to ADMIN role.")
public class UserAdminController {

    private final UserService userService;
    private final UserHistoryService userHistoryService;

    @Operation(summary = "Get filtered users", description = "Retrieves a paginated list of users filtered by role.")
    @GetMapping("/filter")
    public ResponseEntity<Page<UserDto>> getUsers(
            @Parameter(description = "Filter by user role") @RequestParam(required = false) String role,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(userService.findAllByFilter(new UserFilter(role), pageable));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(
            @PathVariable @NotNull @Positive Long id
    ) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Deactivate user", description = "Sets the user status to inactive.")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(
            @PathVariable @NotNull @Positive Long id
    ) {
        userService.deactivate(id);
        return ResponseEntity.ok(Map.of(
                "message", "User with ID %d has been deactivated successfully".formatted(id)
        ));
    }

    @Operation(summary = "Get user history state")
    @GetMapping("/{id}/history")
    public ResponseEntity<UserHistoryDto> history(
            @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "Date in ISO format (YYYY-MM-DD)")
            @RequestParam @NotNull @PastOrPresent LocalDate date
    ) {
        return ResponseEntity.ok(userHistoryService.getUserStateAt(id, date));
    }
}
