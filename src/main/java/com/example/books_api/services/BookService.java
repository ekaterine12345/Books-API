package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.dtos.BookResponseDto;
import com.example.books_api.dtos.FileDownloadDto;
import com.example.books_api.dtos.UpdateBookDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final FileStorageService fileStorageService;

    @Autowired
    public BookService(BookRepository bookRepository, BookMapper bookMapper,
                       UserRepository userRepository, SecurityService securityService,
                       FileStorageService fileStorageService) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.fileStorageService = fileStorageService;
    }

    private Book getBookById(Long id){   // helper method
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));
    }

    private User getCurrentUser() {  // helper method
        String email = securityService.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    
    public BookResponseDto addBook(BookDto bookDto) {
        if (bookDto == null) {
            throw new NullPointerException("Book should not be empty!");
        }

        Book book = new Book(bookDto);
        Book insertedBook = bookRepository.save(book);
        return bookMapper.toResponseDto(insertedBook);
    }

    public BookResponseDto purchaseBook(Long bookId) {
        User user = getCurrentUser();
        Book book = getBookById(bookId);

        if (!user.getPurchasedBooks().contains(book)) {
            user.getPurchasedBooks().add(book);
            userRepository.save(user); // Saving the relationship in the join table
        }

        return  bookMapper.toResponseDto(book);
    }

    public FileDownloadDto downloadBook(Long bookId) throws MalformedURLException {
        User user = getCurrentUser();
        Book book = getBookById(bookId);
        BookFile bookFile = book.getBookFile();

        if (bookFile == null || bookFile.getFilePath() == null) {
            throw new EntityNotFoundException("book file not found for book id: " + bookId);
        }

        // Check if the user owns this book
        if (!user.getPurchasedBooks().contains(book))
             throw new BookAccessDeniedException("You must purchase this book before downloading");

        Resource resource = fileStorageService.loadFileAsResource(bookFile.getFilePath());
        return new FileDownloadDto(resource, bookFile.getFileName(), bookFile.getContentType());
    }

    public List<BookResponseDto> getAllBooks() {
        List<Book> books =  bookRepository.findAll();
        return books.stream()
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public BookResponseDto getBook(Long id){
        return bookMapper.toResponseDto(getBookById(id));
    }

    public BookResponseDto updateBook(Long id, UpdateBookDto bookDto) {
        Book book = getBookById(id);
        bookMapper.updateBookFromDto(bookDto, book);
        return bookMapper.toResponseDto(bookRepository.save(book));
    }

    public BookResponseDto deleteBookById(Long id){
        Book book = getBookById(id);
        if (book.getBookFile() != null){
            fileStorageService.deleteFile(book.getBookFile().getFilePath());
        }

        bookRepository.deleteById(id);

        return bookMapper.toResponseDto(book);
    }

    public BookResponseDto updateBookFile(Long bookId, MultipartFile bookPhysicalFile) throws IOException {
        if (bookPhysicalFile == null || bookPhysicalFile.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!"application/pdf".equals(bookPhysicalFile.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        Book book = getBookById(bookId);

        String storedPath = fileStorageService.saveFile(bookId, bookPhysicalFile);
        BookFile bookFile = book.getBookFile();

        if (bookFile == null) {
            bookFile = new BookFile();
            bookFile.setBook(book);
        }

        bookFile.setFileName(bookPhysicalFile.getOriginalFilename());
        bookFile.setFilePath(storedPath);
        bookFile.setContentType(bookPhysicalFile.getContentType());

        book.setBookFile(bookFile);

        bookRepository.save(book);

        return bookMapper.toResponseDto(book);
    }
}
