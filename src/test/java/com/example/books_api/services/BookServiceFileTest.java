package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.dtos.BookResponseDto;
import com.example.books_api.dtos.FileDownloadDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.BookFile;
import com.example.books_api.entities.User;
import com.example.books_api.exceptions.BookAccessDeniedException;
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
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceFileTest {
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

    // =================== Download ============================================================
    @Test
    void shouldThrowWhenDownloadingAndUserNotFound() {
        // not successful download - reason: user not found

        String userEmail =  "test@test.com";
        when(securityService.getCurrentUserEmail())
                .thenReturn(userEmail);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> bookService.downloadBook(1L));
    }

    @Test
    void shouldThrowWhenDownloadingAndBookNotFound() {
        // not successful download - reason: book not found
        String userEmail =  "test@test.com";
        when(securityService.getCurrentUserEmail()).thenReturn(userEmail);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.downloadBook(1L));
    }

    @Test
    void shouldThrowWhenBookFileMissing() {
        String userEmail = "test@test.com";
        when(securityService.getCurrentUserEmail()).thenReturn(userEmail);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        Book book = new Book();
        book.setId(1L);
        book.setBookFile(null);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        assertThrows(EntityNotFoundException.class,
                () -> bookService.downloadBook(1L));
    }
    @Test
    void shouldThrowWhenBookNotPurchased() throws MalformedURLException { // shouldReturnForbiddenWhenBookNotPurchased
        // not successful download - reason: book not purchased
        //  Given
        String userEmail =  "test@test.com";
        String filePath = "path/spring.pdf", fileName = "spring.pdf", fileContentType = "application/pdf";

        when(securityService.getCurrentUserEmail()).thenReturn(userEmail);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        Book book = new Book();
        book.setId(1L);

        BookFile bookFile = new BookFile();
        bookFile.setFileName(fileName);
        bookFile.setContentType(fileContentType);
        bookFile.setFilePath(filePath);

        book.setBookFile(bookFile);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        assertThatThrownBy(() ->  bookService.downloadBook(1L))
                .isInstanceOf(BookAccessDeniedException.class)
                .hasMessage("You must purchase this book before downloading");
    }

    @Test
    void shouldDownloadBookSuccessfully() throws MalformedURLException {
        // Successful download
        // Given
        Long bookId = 1L;
        String userEmail =  "test@test.com";
        String filePath = "path/spring.pdf", fileName = "spring.pdf", fileContentType = "application/pdf";

        when(securityService.getCurrentUserEmail()).thenReturn(userEmail);

        Book book = new Book();
        book.setId(bookId);

        BookFile bookFile = new BookFile();
        bookFile.setFileName(fileName);
        bookFile.setContentType(fileContentType);
        bookFile.setFilePath(filePath);

        book.setBookFile(bookFile);

        Resource mockResource = mock(Resource.class);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());
        user.getPurchasedBooks().add(book);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(fileStorageService.loadFileAsResource(filePath)).thenReturn(mockResource);

        // When
        FileDownloadDto result = bookService.downloadBook(bookId);

        // Then
        assertThat(result).isNotNull();

        assertThat(result.getResource()).isEqualTo(mockResource);
        assertThat(result.getFileName()).isEqualTo(fileName);
        assertThat(result.getContentType()).isEqualTo(fileContentType);

        verify(fileStorageService).loadFileAsResource(filePath);
    }

    // ======================= update Book File ==========================================================
    @Test
    void shouldThrowWhenFileIsNull() {
        assertThatThrownBy(() -> bookService.updateBookFile(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File is empty");
    }

    @Test
    void shouldThrowWhenFileIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBookFile(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File is empty");
    }

    @Test
    void shouldThrowWhenFileIsNotPdf() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");

        assertThatThrownBy(() -> bookService.updateBookFile(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only PDF files are allowed");
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBookFile(1L, file))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    void shouldUploadFileWhenBookHasNoExistingFile() throws IOException, IOException {
        // Given
        Long bookId = 1L;
        String fileName = "book.pdf", contentType = "application/pdf";
        String storedPath = "stored/path/book.pdf";

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getOriginalFilename()).thenReturn(fileName);

        Book book = new Book();
        book.setId(bookId);
        book.setBookFile(null);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(fileStorageService.saveFile(bookId, file)).thenReturn(storedPath);
        when(bookRepository.save(book)).thenReturn(book);

        BookResponseDto responseDto = new BookResponseDto();
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        // When
        BookResponseDto result = bookService.updateBookFile(bookId, file);

        // Then
        assertThat(result).isEqualTo(responseDto);

        assertThat(book.getBookFile()).isNotNull();
        assertThat(book.getBookFile().getFileName()).isEqualTo(fileName);
        assertThat(book.getBookFile().getFilePath()).isEqualTo(storedPath);
        assertThat(book.getBookFile().getContentType()).isEqualTo(contentType);

        verify(fileStorageService).saveFile(bookId, file);
        verify(bookRepository).save(book);
    }

    @Test
    void shouldUpdateExistingBookFile() throws IOException {
        // Given
        Long bookId = 1L;
        String oldFileName = "old_book.pdf", newFileName = "new_book.pdf", contentType = "application/pdf";
        String storedPath = "stored/path/new.pdf";

        MultipartFile file = mock(MultipartFile.class); // new file
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getOriginalFilename()).thenReturn(newFileName);

        BookFile existingFile = new BookFile();
        existingFile.setFileName(oldFileName);

        Book book = new Book();
        book.setId(bookId);
        book.setBookFile(existingFile);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(fileStorageService.saveFile(bookId, file)).thenReturn(storedPath);
        when(bookRepository.save(book)).thenReturn(book);

        BookResponseDto responseDto = new BookResponseDto();
        when(bookMapper.toResponseDto(book)).thenReturn(responseDto);

        // When
        BookResponseDto result = bookService.updateBookFile(bookId, file);

        // Then
        assertThat(result).isEqualTo(responseDto);

        assertThat(existingFile.getFileName()).isEqualTo(newFileName);
        assertThat(existingFile.getFilePath()).isEqualTo(storedPath);
        assertThat(existingFile.getContentType()).isEqualTo(contentType);

        verify(fileStorageService).saveFile(bookId, file);
        verify(bookRepository).save(book);
    }
}
