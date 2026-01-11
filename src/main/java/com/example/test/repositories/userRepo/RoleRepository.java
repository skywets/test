package com.example.test.repositories.userRepo;

import com.example.test.models.entities.user.RoleT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleT, Long> {

    Optional<RoleT> findByName(String name);

    boolean existsByName(String name);
}

