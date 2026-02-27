package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.dtos.BookResponseDto;
import com.example.books_api.dtos.UpdateBookDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.BookFile;
import com.example.books_api.entities.User;
import com.example.books_api.exceptions.BookNotFoundException;
import com.example.books_api.exceptions.UserNotFoundException;
import com.example.books_api.mapper.BookMapper;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    // this Book Service Test is focused on CRUD + Purchase
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private SecurityService securityService;


    // =================== Add book ============================================================
    @Test
    void shouldAddBookSuccessfully() {
        // Adding book
        BookDto dto = new BookDto("Spring", "John", 1990,
                120, 20.0, "descrption ...");

        Book book = new Book(dto);

        BookResponseDto savedDto = new BookResponseDto(1L, "Spring", "John", 1990,
                120, 20.0, "descrption ...", "book.pdf");

        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toResponseDto(book)).thenReturn(savedDto);

        BookResponseDto response = bookService.addBook(dto);

        assertThat(response).isEqualTo(savedDto);
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

        assertThrows(UserNotFoundException.class,
                () -> bookService.purchaseBook(1L));
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        // not successful purchase - reason: book not found

        String userEmail =  "test@test.com";

        when(securityService.getCurrentUserEmail())
                .thenReturn(userEmail);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.purchaseBook(1L));
    }


    @Test
    void shouldPurchaseBookSuccessfully() {
        // successful purchase
        Long bookId = 1L;
        String userEmail =  "test@test.com";
        when(securityService.getCurrentUserEmail())
                .thenReturn(userEmail);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        Book book = new Book();
        book.setId(bookId);

        BookResponseDto responseDto = new BookResponseDto();
        responseDto.setId(bookId);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        BookResponseDto response = bookService.purchaseBook(bookId);


        assertThat(response).isEqualTo(responseDto);
        assertEquals(1, user.getPurchasedBooks().size());

        verify(userRepository).save(user);
    }


    // =================== update book ============================================================
    @Test
    void shouldUpdateBookSuccessfully() {
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);

        UpdateBookDto dto = new UpdateBookDto("Updated", "Author", 1990,
                120, 200.0, "description ...");

        BookResponseDto responseDto = new BookResponseDto(bookId, "Updated", "Author", 1990,
                120, 200.0, "description ...", "book.pdf");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        BookResponseDto  response = bookService.updateBook(1L, dto);

        assertThat(response).isEqualTo(responseDto);

        verify(bookMapper).updateBookFromDto(dto, book);
        verify(bookRepository).save(book);
    }


    @Test
    void shouldReturnErrorWhenUpdatingMissingBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(1L, new UpdateBookDto()))
                .isInstanceOf(BookNotFoundException.class);
    }


    // =================== delete book ============================================================
    @Test
    void shouldDeleteBookSuccessfully() {
        // without book file
        // Given
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        book.setBookFile(null);

        BookResponseDto bookResponseDto = new BookResponseDto();

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));
        when(bookMapper.toResponseDto(book)).thenReturn(bookResponseDto);

        // When
        BookResponseDto response = bookService.deleteBookById(1L);

        // Then
        assertThat(response).isEqualTo(bookResponseDto);

        verify(bookRepository).deleteById(1L);
        verify(fileStorageService, never()).deleteFile(any());
    }

    @Test
    void shouldDeleteBookAndPhysicalFileIfExists(){
        // Given
        Long bookId = 1L;
        String filePath = "path/my_book.pdf";
        Book book = new Book();
        book.setId(bookId);

        BookFile bookFile = new BookFile();
        bookFile.setFilePath(filePath);
        book.setBookFile(bookFile);

        BookResponseDto responseDto = new BookResponseDto();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toResponseDto(book))
                .thenReturn(responseDto);

        // When
        BookResponseDto result = bookService.deleteBookById(bookId);

        // Then
        verify(fileStorageService).deleteFile(filePath);
        verify(bookRepository).deleteById(bookId);
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void shouldThrowIfBookNotFoundForDelete() { // shouldReturnErrorWhenDeletingMissingBook()
        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBookById(1L))
                .isInstanceOf(BookNotFoundException.class);
    }
}
