package com.example.test.security;

import com.example.test.models.entities.notification.Notification;
import com.example.test.repositories.notiRepo.NotificationRepository;
import com.example.test.services.userService.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("notificationSecurity")
@RequiredArgsConstructor
public class NotificationSecurity {
    private final NotificationRepository repository;

    public boolean isOwner(Long notificationId, Authentication authentication) {
        Notification n = repository.findById(notificationId).orElse(null);
        if (n == null) return false;

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return n.getUser().getId().equals(principal.getUser().getId());
    }
}

