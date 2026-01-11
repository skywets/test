package com.example.test.init;

import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepo,
                                       RoleRepository roleRepo,
                                       PasswordEncoder encoder) {
        return args -> {

            if (userRepo.findByEmail("admin@example.com").isPresent()) {
                return;
            }

            RoleT adminRole = roleRepo.findByName("ADMIN")
                    .orElseGet(() -> {
                        RoleT r = new RoleT();
                        r.setName("ADMIN");
                        return roleRepo.save(r);
                    });

            User admin = User.builder()
                    .name("Admin")
                    .email("admin@example.com")
                    .password(encoder.encode("AdminPass123!"))
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            admin.getRoleTSet().add(adminRole);

            userRepo.save(admin);

            System.out.println("ADMIN initialized!");
        };
    }
}
