package com.example.test.services.courierService.impl;

import com.example.test.exceptions.ApplicationAlreadyExistsException;
import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.courierDto.CourierApplicationDto;
import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.courier.CourierApplication;
import com.example.test.models.entities.enums.CourierStatus;
import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.courierMapper.CourierApplicationMapper;
import com.example.test.repositories.courierRepo.CourierApplicationRepository;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.courierService.CourierApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourierApplicationServiceImpl implements CourierApplicationService {

    private final CourierApplicationRepository courierApplicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CourierRepository courierRepository;
    private final CourierApplicationMapper courierApplicationMapper;

    @Override
    @Transactional
    public CourierApplicationDto createApplication(CourierApplicationDto dto) {
        Long userId = dto.getUserId();

        if (courierRepository.existsByUserId(userId)) {
            throw new IllegalStateException("User is already a courier");
        }

        if (courierApplicationRepository.existsByUserIdAndStatus(userId, Status.PENDING)) {
            throw new ApplicationAlreadyExistsException("You already have a pending courier application");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        CourierApplication app = CourierApplication.builder()
                .user(user)
                .documents(dto.getDocuments())
                .status(Status.PENDING)
                .build();

        return courierApplicationMapper.toDto(courierApplicationRepository.save(app));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierApplicationDto> getByUser(Long userId) {
        List<CourierApplication> apps = courierApplicationRepository.findAllByUserId(userId);

        if (apps.isEmpty()) {
            throw new NotFoundException("You have no courier applications");
        }

        return apps.stream()
                .map(courierApplicationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void approve(Long applicationId, Long adminId) {
        CourierApplication app = courierApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (app.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Application has already been processed and is in status: " + app.getStatus());
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found with ID: " + adminId));

        User user = app.getUser();

        Courier courier = Courier.builder()
                .user(user)
                .status(CourierStatus.OFFLINE)
                .build();
        courierRepository.save(courier);

        RoleT courierRole = roleRepository.findByName("COURIER")
                .orElseThrow(() -> new IllegalStateException("Critical error: Role COURIER not found in database"));

        if (!user.getRoleTSet().contains(courierRole)) {
            user.getRoleTSet().add(courierRole);
            userRepository.save(user);
        }

        app.setStatus(Status.APPROVED);
        app.setProcessedAt(LocalDateTime.now());
        app.setProcessedByAdmin(admin);

        courierApplicationRepository.save(app);
    }

    @Override
    @Transactional
    public void reject(Long applicationId, Long adminId, String comment) {
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Rejection comment cannot be empty");
        }

        CourierApplication app = courierApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (app.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Application is already processed");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        app.setStatus(Status.REJECTED);
        app.setAdminComment(comment);
        app.setProcessedAt(LocalDateTime.now());
        app.setProcessedByAdmin(admin);

        courierApplicationRepository.save(app);
    }

    @Override
    @Transactional(readOnly = true)
    public CourierApplicationDto getById(Long id) {
        return courierApplicationRepository.findById(id)
                .map(courierApplicationMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Courier application not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierApplicationDto> getAll() {
        return courierApplicationRepository.findAll().stream()
                .map(courierApplicationMapper::toDto)
                .toList();
    }

}