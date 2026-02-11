package com.example.books_api.respsitories;

import com.example.books_api.entities.Book;
import com.example.books_api.entities.User;
import com.example.books_api.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User should purchase book")
    void shouldSaveUserWithPurchasedBook() {

        Book book = new Book();
        book.setTitle("Spring");
        book.setAuthor("John");
        book.setPrice(20.0);

        bookRepository.save(book);

        User user = User.builder()
                .firstname("Anna")
                .lastname("Smith")
                .email("anna@test.com")
                .password("123")
                .role(Role.USER)
                .build();

        user.getPurchasedBooks().add(book);

        userRepository.save(user);

        User found = userRepository.findByEmail("anna@test.com").get();

        assertThat(found.getPurchasedBooks()).hasSize(1);
    }

}