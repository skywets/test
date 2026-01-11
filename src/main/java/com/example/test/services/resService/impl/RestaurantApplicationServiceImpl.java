package com.example.test.services.resService.impl;

import com.example.test.exceptions.ApplicationAlreadyExistsException;
import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.restaurantDto.RestaurantApplicationDto;
import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.restaurant.RestaurantApplication;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.resMapper.RestaurantApplicationMapper;
import com.example.test.repositories.resRepo.RestaurantApplicationRepository;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.resService.RestaurantApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantApplicationServiceImpl implements RestaurantApplicationService {

    private final RestaurantApplicationRepository restaurantApplicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantApplicationMapper restaurantApplicationMapper;


    @Override
    public RestaurantApplicationDto createApplication(RestaurantApplicationDto dto) {

        if (restaurantApplicationRepository
                .existsByUserIdAndStatus(dto.getUserId(), Status.PENDING)) {
            throw new ApplicationAlreadyExistsException(
                    "You already have a pending application"
            );
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RestaurantApplication app = RestaurantApplication.builder()
                .user(user)
                .documents(dto.getDocuments())
                .status(Status.PENDING)
                .build();

        return restaurantApplicationMapper.toDto(
                restaurantApplicationRepository.save(app)
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<RestaurantApplicationDto> getByUser(Long userId) {

        List<RestaurantApplication> apps =
                restaurantApplicationRepository.findAllByUserId(userId);

        if (apps.isEmpty()) {
            throw new NotFoundException("You have not submitted any restaurant applications yet");
        }

        return apps.stream()
                .map(restaurantApplicationMapper::toDto)
                .toList();
    }


    @Transactional
    public void approve(Long applicationId, Long adminId) {

        RestaurantApplication app =
                restaurantApplicationRepository.findById(applicationId)
                        .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != Status.PENDING) {
            throw new RuntimeException("Application already processed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        User user = app.getUser();

        RoleT ownerRole = roleRepository.findByName("RESTAURANT_OWNER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (user.getRoleTSet().stream().noneMatch(r -> r.getName().equals("RESTAURANT_OWNER"))) {
            user.getRoleTSet().add(ownerRole);
            userRepository.save(user);
        }

        app.setStatus(Status.APPROVED);
        app.setProcessedAt(LocalDateTime.now());
        app.setProcessedByAdmin(admin);

        restaurantApplicationRepository.save(app);
    }

    @Override
    @Transactional
    public void reject(Long applicationId, Long adminId, String comment) {
        RestaurantApplication app = restaurantApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new RuntimeException("Admin with ID %s not found".formatted(adminId))
                );

        if (app.getStatus() != Status.PENDING) {
            throw new RuntimeException("Application already processed");
        }

        app.setStatus(Status.REJECTED);
        app.setAdminComment(comment);
        app.setProcessedAt(LocalDateTime.now());
        app.setProcessedByAdmin(admin);

        restaurantApplicationRepository.save(app);
    }

}
