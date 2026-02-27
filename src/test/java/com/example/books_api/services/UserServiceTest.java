package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.BookResponseDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.User;
import com.example.books_api.exceptions.UserNotFoundException;
import com.example.books_api.mapper.BookMapper;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        // Given
        String userEmail = "jane@test.com";
        String firstName = "Jane";

        when(securityService.getCurrentUserEmail()).thenReturn(userEmail);

        Book book1 = new Book();
        book1.setId(1L);

        Book book2 = new Book();
        book2.setId(2L);

        User user = new User();
        user.setFirstname(firstName);
        user.setPurchasedBooks(new ArrayList<>(List.of(book1, book2)));

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        when(bookMapper.toResponseDto(book1)).thenReturn(new BookResponseDto());
        when(bookMapper.toResponseDto(book2)).thenReturn(new BookResponseDto());

        // When
        ApiResponse response = userService.getPurchasedBooks();

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey(firstName+"'s purchased books"));

        List<?> books = (List<?>) response.getData()
                .get(firstName+"'s purchased books");

        // Then
        assertEquals(2, books.size());

        verify(userRepository).findByEmail(userEmail);
        verify(bookMapper, times(2)).toResponseDto(any(Book.class));
    }

    // ==================   PurchasedBooks NOT Success - user not found =======================================
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String useEmail = "missing@test.com";

        when(securityService.getCurrentUserEmail())
                .thenReturn(useEmail);

        when(userRepository.findByEmail(useEmail))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getPurchasedBooks()
        );

        assertEquals("User not found", exception.getMessage());
    }

    // ==================   PurchasedBooks  - purchase list is empty =======================================
    @Test
    void shouldReturnEmptyListWhenUserHasNoPurchasedBooks() {
        String userEmail = "user@test.com";
        String firstName = "Anna";

        when(securityService.getCurrentUserEmail())
                .thenReturn(userEmail);

        User user = new User();
        user.setFirstname(firstName);
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        ApiResponse response = userService.getPurchasedBooks();

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey(firstName +"'s purchased books"));

        List<?> books = (List<?>) response.getData()
                .get(firstName + "'s purchased books");

        assertTrue(books.isEmpty());

        verify(bookMapper, never()).toResponseDto(any());
    }
}
