package com.example.test.config;

import com.example.test.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                        .disable()
                )
                .cors(Customizer.withDefaults())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()

                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/reviews").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/menu-items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/food-types/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()

                        .requestMatchers("/api/v1/profile/**").authenticated()

                        .requestMatchers("/api/v1/courier/profile/**").hasRole("COURIER")

                        .requestMatchers("/api/v1/restaurant/profile/**").hasRole("RESTAURANT_OWNER")

                        .requestMatchers("/api/v1/restaurants/*/menu/**").hasRole("RESTAURANT_OWNER")

                        .requestMatchers("/api/v1/cart/**").hasRole("USER")
                        .requestMatchers("/api/v1/orders/my").hasRole("USER")
                        .requestMatchers("/api/v1/applications/couriers/**").hasRole("USER")
                        .requestMatchers("/api/v1/applications/restaurants/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").hasRole("USER")

                        .requestMatchers("/api/v1/couriers/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/payments/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/restaurant/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/food-types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/menu-items/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )


                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http:localhost:8082"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
        ));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
