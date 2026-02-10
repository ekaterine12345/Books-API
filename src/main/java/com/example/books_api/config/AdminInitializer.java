package com.example.books_api.config;


import com.example.books_api.entities.User;
import com.example.books_api.respsitories.UserRepository;
import com.example.books_api.user.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createAdmin() {
        if (userRepository.findByEmail("admin@books.com").isEmpty()) {
            User admin = User.builder()
                    .firstname("Admin")
                    .lastname("User")
                    .email("admin@books.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
