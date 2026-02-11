package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.User;
import com.example.books_api.mapper.BookMapper;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private UserService userService;

    @Mock
    private SecurityService securityService;

    // ==================   PurchasedBooks - Success =======================================
    @Test
    void shouldReturnPurchasedBooksSuccessfully() {

        String user_email = "user@test.com";
        String user_firstName = "Jane";

        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        Book book1 = new Book();
        book1.setId(1L);

        Book book2 = new Book();
        book2.setId(2L);

        User user = new User();
        user.setFirstname(user_firstName);
        user.setPurchasedBooks(new ArrayList<>(List.of(book1, book2)));

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.of(user));

        when(bookMapper.toDto(book1)).thenReturn(new BookDto());
        when(bookMapper.toDto(book2)).thenReturn(new BookDto());

        ApiResponse response = userService.getPurchasedBooks();

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey(user_firstName+"'s purchased books"));

        List<?> books = (List<?>) response.getData()
                .get(user_firstName+"'s purchased books");

        assertEquals(2, books.size());

        verify(userRepository).findByEmail(user_email);
        verify(bookMapper, times(2)).toDto(any(Book.class));
    }

    // ==================   PurchasedBooks NOT Success - user not found =======================================
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String user_email = "missing@test.com";

        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getPurchasedBooks()
        );

        assertEquals("User not found", exception.getMessage());
    }

    // ==================   PurchasedBooks  - purchase list is empty =======================================
    @Test
    void shouldReturnEmptyListWhenUserHasNoPurchasedBooks() {
        String user_email = "user@test.com";
        String user_firstName = "Anna";

    ///    mockAuthenticatedUser(user_email);

        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        User user = new User();
        user.setFirstname(user_firstName);
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.of(user));

        ApiResponse response = userService.getPurchasedBooks();

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey(user_firstName+"'s purchased books"));

        List<?> books = (List<?>) response.getData()
                .get(user_firstName + "'s purchased books");

        assertTrue(books.isEmpty());

        verify(bookMapper, never()).toDto(any());
    }
}

