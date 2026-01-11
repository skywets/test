package com.example.test.controllers.restaurant;

import com.example.test.models.dtos.restaurantDto.RestaurantApplicationDto;
import com.example.test.models.entities.user.User;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.resService.RestaurantApplicationService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/applications/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Restaurant Applications", description = "Endpoints for users to apply for restaurant partnership")
public class RestaurantApplicationController {

    private final RestaurantApplicationService restaurantApplicationService;
    private final UserRepository userRepo;

    @Operation(summary = "Submit a restaurant application", description = "Allows an authenticated user to submit an application to register a restaurant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application submitted successfully",
                    content = @Content(schema = @Schema(implementation = RestaurantApplicationDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid application data provided"),
            @ApiResponse(responseCode = "403", description = "Forbidden: User role required")
    })
    @PostMapping
    public RestaurantApplicationDto create(
            @Valid @RequestBody RestaurantApplicationDto dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        dto.setUserId(user.getId());
        return restaurantApplicationService.createApplication(dto);
    }

    @Operation(summary = "Get my applications", description = "Retrieves a list of restaurant applications submitted by the current user.")
    @GetMapping
    public List<RestaurantApplicationDto> myApplications(@Parameter(hidden = true) Authentication auth) {
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        return restaurantApplicationService.getByUser(
                principal.getUser().getId()
        );
    }
}
