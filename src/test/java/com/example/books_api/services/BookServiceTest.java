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
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Mock
    private SecurityService securityService;


    // =================== Add book ============================================================
    @Test
    void shouldAddBookSuccessfully() {
        // Adding book
        BookDto dto = new BookDto("Spring", "John", 1990,
                120, 20.0, "descrption ...", "book.pdf");

        Book savedBook = new Book(dto);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        ApiResponse response = bookService.addBook(dto);


        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey("New Book"));
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void shouldThrowExceptionWhenBookDtoIsNull() {
        // Adding book null Case
        assertThrows(NullPointerException.class, () -> bookService.addBook(null));
    }

    // =================== purchase book ============================================================
    @Test
    void shouldThrowWhenUserNotFound() {
        // not successful purchase - reason: user not found

        when(securityService.getCurrentUserEmail())
                .thenReturn("test@test.com");

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> bookService.purchaseBook(1L));
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        // not successful purchase - reason: book not found

        String user_email =  "test@test.com";

        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> bookService.purchaseBook(1L));
    }


    @Test
    void shouldPurchaseBookSuccessfully() {
        // successful purchase
        String user_email =  "test@test.com";
        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        Book book = new Book();
        book.setId(1L);

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        ApiResponse response = bookService.purchaseBook(1L);

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey("book purchased "));
        assertEquals(1, user.getPurchasedBooks().size());

        verify(userRepository).save(user);
    }



    // =================== update book ============================================================
    // update book
    @Test
    void shouldUpdateBookSuccessfully() {
        Book book = new Book();
        BookDto dto = new BookDto("Updated", "Author", 1990,
                120, 200.0, "descrption ...", "book.pdf");

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(book))
                .thenReturn(book);

        ApiResponse response = bookService.updateBook(1L, dto);

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey("updated book"));

        verify(bookMapper).updateBookFromDto(dto, book);
    }


    @Test
    void shouldReturnErrorWhenUpdatingMissingBook() {
        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        ApiResponse response =
                bookService.updateBook(1L, new BookDto());

        assertTrue(response.getData().isEmpty());
        assertTrue(response.getError().containsKey("id"));
    }


    // =================== delete book ============================================================
    @Test
    void shouldDeleteBookSuccessfully() {
        Book book = new Book();

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        ApiResponse response =
                bookService.deleteBookById(1L);

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey("deleted book"));

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void shouldReturnErrorWhenDeletingMissingBook() {
        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        ApiResponse response =
                bookService.deleteBookById(1L);

        assertTrue(response.getData().isEmpty());
        assertTrue(response.getError().containsKey("id"));
    }


    // =================== Download ============================================================
    @Test
    void shouldThrowWhenDownloadingAndUserNotFound() {
        // not successful download - reason: user not found

        String user_email =  "test@test.com";
        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

      //  mockAuthenticatedUser("missing@test.com");

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> bookService.downloadBook(1L));
    }

    @Test
    void shouldThrowWhenDownloadingAndBookNotFound() {
        // not successful download - reason: book not found
      //  mockAuthenticatedUser("test@test.com");
        String user_email =  "test@test.com";
        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> bookService.downloadBook(1L));
    }

    @Test
    void shouldReturnForbiddenWhenBookNotPurchased() {
        // not successful download - reason: book not purchased
      //  mockAuthenticatedUser("test@test.com");
        String user_email =  "test@test.com";
        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        Book book = new Book();
        book.setId(1L);
        book.setBookFileName("file.pdf");

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        var response = bookService.downloadBook(1L);

        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody()
                .contains("You must purchase"));
    }

    @Test
    void shouldDownloadBookSuccessfully() {
        // Successful download
      ///////////  mockAuthenticatedUser("test@test.com");
        String user_email =  "test@test.com";
        when(securityService.getCurrentUserEmail())
                .thenReturn(user_email);

        Book book = new Book();
        book.setId(1L);
        book.setBookFileName("spring.pdf");

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());
        user.getPurchasedBooks().add(book);

        when(userRepository.findByEmail(user_email))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        var response = bookService.downloadBook(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody()
                .contains("Access Granted"));
    }




}
