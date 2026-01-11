package com.example.test.security;

import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.services.userService.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourierSecurity {

    private final CourierRepository courierRepository;

    public boolean isOwner(Long courierId, Authentication authentication) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();

        return courierRepository
                .findById(courierId)
                .map(c -> c.getUser().getId().equals(principal.getUser().getId()))
                .orElse(false);
    }
}
