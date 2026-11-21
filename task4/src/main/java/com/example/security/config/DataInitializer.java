package com.example.security.config;

import com.example.security.model.AppUser;
import com.example.security.model.Permission;
import com.example.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/** Seeds demo users (passwords stored as salted BCrypt hashes). */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            seed(repo, encoder, "admin@example.com", "Admin#123", Set.of(Permission.VIEW_INFO, Permission.VIEW_ADMIN));
            seed(repo, encoder, "info@example.com", "Info#123", Set.of(Permission.VIEW_INFO));
            seed(repo, encoder, "adminonly@example.com", "AdminOnly#123", Set.of(Permission.VIEW_ADMIN));
            // 4th user reserved for brute-force testing so other users never get blocked
            seed(repo, encoder, "locktest@example.com", "LockTest#123", Set.of(Permission.VIEW_INFO));
        };
    }

    private void seed(UserRepository repo, PasswordEncoder encoder, String email, String pwd, Set<Permission> perms) {
        if (repo.findByEmailIgnoreCase(email).isEmpty()) {
            repo.save(new AppUser(email, encoder.encode(pwd), perms));
        }
    }
}
