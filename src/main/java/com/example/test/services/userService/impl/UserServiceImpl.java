package com.example.test.services.userService.impl;

import com.example.test.exceptions.ConflictException;
import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.dtos.userDto.UserFilter;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import com.example.test.models.mappers.userMapper.UserHistoryMapper;
import com.example.test.models.mappers.userMapper.UserMapper;
import com.example.test.models.mappers.userMapper.UserRegisterMapper;
import com.example.test.repositories.userRepo.UserHistoryRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.userService.UserService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserRegisterMapper userRegisterMapper;
    private final UserHistoryRepository historyRepository;
    private final UserHistoryMapper historyMapper;


    @Override
    public void create(UserRegisterDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("UserRegisterDto must not be null");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = userRegisterMapper.toModel(dto);
        User savedUser = userRepository.save(user);
        UserHistory history = historyMapper.toEntity(savedUser);
        history.setUserId(user.getId());
        historyRepository.save(history);
    }


    @Override
    public UserDto findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }
        return userMapper.toDTO(getByIdOrElseThrow(id));
    }


    @Override
    public Page<UserDto> findAllByFilter(UserFilter filter, Pageable pageable) {

        return userRepository.findAll(
                (Specification<User>) (root, query, cb) -> {

                    List<Predicate> predicates = new ArrayList<>();

                    if (filter.role() != null && !filter.role().isBlank()) {

                        Join<User, RoleT> roleJoin =
                                root.join("roleTSet", JoinType.INNER);

                        predicates.add(
                                cb.equal(roleJoin.get("name"), filter.role())
                        );
                    }

                    query.distinct(true);

                    return cb.and(predicates.toArray(Predicate[]::new));
                },
                pageable
        ).map(userMapper::toDTO);
    }


    @Override
    public void deactivate(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setActive(false);

        User savedUser = userRepository.save(user);
        UserHistory history = historyMapper.toEntity(savedUser);
        history.setUserId(user.getId());
        historyRepository.save(history);

    }


    private User getByIdOrElseThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}