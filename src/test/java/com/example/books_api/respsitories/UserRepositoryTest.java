package com.example.books_api.respsitories;

import com.example.books_api.entities.User;
import com.example.books_api.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and find user by email")
    void shouldFindUserByEmail() {

        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@test.com")
                .password("password")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        Optional<User> found =
                userRepository.findByEmail("john@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstname()).isEqualTo("John");
        assertThat(found.get().getRole()).isEqualTo(Role.USER);
    }
}