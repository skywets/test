package com.example.test.models.entities.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<PermissionT> permissionTSet = new HashSet<>();

    public List<SimpleGrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> permissionAuthorities = permissionTSet.stream()
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .collect(Collectors.toList());

        permissionAuthorities.add(new SimpleGrantedAuthority("ROLE_" + this.name));

        return permissionAuthorities;
    }
}
