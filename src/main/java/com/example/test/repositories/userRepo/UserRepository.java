package com.example.test.repositories.userRepo;

import com.example.test.models.entities.user.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
