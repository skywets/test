package com.example.test.services.userService.impl;

import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final User user;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getRoleTSet() != null) {
            for (RoleT role : user.getRoleTSet()) {
                authorities.addAll(role.getAuthorities());
            }
        }


        System.out.println("Authorities = " + authorities);
        return authorities;
    }


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
