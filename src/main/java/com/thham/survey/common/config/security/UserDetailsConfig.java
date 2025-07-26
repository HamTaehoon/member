package com.thham.survey.common.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final BCryptPasswordEncoder passwordEncoder;

    @Bean
    public UserDetailsService userDetailsService() {
        log.debug("Creating admin UserDetails with BCrypt encoded password");
        String encodedPassword = passwordEncoder.encode("1212");
        log.debug("Encoded password for admin: {}", encodedPassword);
        UserDetails admin = User.withUsername("admin")
                .password(encodedPassword)
                .roles("ADMIN")
                .build();

        InMemoryUserDetailsManager adminManager = new InMemoryUserDetailsManager(admin);

        return username -> {
            log.debug("Authenticating user: {}", username);

            if ("admin".equals(username)) {
                log.debug("Returning admin user from InMemoryUserDetailsManager");
                return adminManager.loadUserByUsername(username);
            }

            log.warn("Non-admin login attempt: {}", username);
            throw new UsernameNotFoundException("Only admin login is allowed: " + username);
        };
    }
}