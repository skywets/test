package com.example.test.repositories.userRepo;

import com.example.test.models.entities.user.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    Optional<UserHistory>
    findFirstByUserIdAndUpdateTimeLessThanEqualOrderByUpdateTimeDesc(
            Long userId,
            LocalDateTime dateTime
    );

}