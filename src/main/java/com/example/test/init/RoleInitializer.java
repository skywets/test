package com.example.test.init;

import com.example.test.models.entities.user.RoleT;
import com.example.test.repositories.userRepo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepo;

    @Bean
    public CommandLineRunner initRoles() {
        return args -> {

            String[] roles = {
                    "USER",
                    "COURIER",
                    "RESTAURANT_OWNER",
                    "ADMIN"
            };

            for (String r : roles) {
                if (!roleRepo.existsByName(r)) {
                    roleRepo.save(RoleT.builder().name(r).build());
                }
            }

            System.out.println("Roles initialized");
        };
    }
}
