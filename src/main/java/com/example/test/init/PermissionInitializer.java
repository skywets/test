package com.example.test.init;

import com.example.test.models.entities.user.PermissionT;
import com.example.test.repositories.userRepo.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PermissionInitializer {
    private final PermissionRepository permissionRepo;

    @Bean
    public CommandLineRunner initPermissions() {
        return args -> {

            String[] permissions = {
                    "USER_READ",
                    "USER_WRITE",
                    "ORDER_READ",
                    "ORDER_WRITE",
                    "MENU_READ",
                    "MENU_WRITE",
                    "RESTAURANT_READ",
                    "RESTAURANT_WRITE",
                    "COURIER_ASSIGN",
                    "PAYMENT_MANAGE",
                    "ADMIN_FULL"
            };

            for (String p : permissions) {
                if (!permissionRepo.existsByName(p)) {
                    permissionRepo.save(PermissionT.builder().name(p).build());
                }
            }

            System.out.println("Permissions initialized");
        };
    }
}
