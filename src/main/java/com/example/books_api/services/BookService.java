package com.example.books_api.services;

import com.example.books_api.BookMapper;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.User;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final UserRepository userRepository;

    @Autowired
    public BookService(BookRepository bookRepository, BookMapper bookMapper, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.userRepository = userRepository;
    }

    
    public ApiResponse addBook(BookDto bookDto) {
        if (bookDto == null) {
            throw new NullPointerException("Book should not be empty!");
        }

        Book book = new Book(bookDto);
        Book insertedBook = bookRepository.save(book);
        return new ApiResponse("New Book", insertedBook);
    }

    public ApiResponse purchaseBook(Long bookId) {
        // Getting the  current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // 3. Check if already purchased to avoid duplicates
        if (!user.getPurchasedBooks().contains(book)) {
            user.getPurchasedBooks().add(book);
            userRepository.save(user); // Saving the relationship in the join table
        }

        return new ApiResponse("book purchased ", book);
    }

    public ResponseEntity<String> downloadBook(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).get();
        Book book = bookRepository.findById(id).get();

        // Check if the user owns this book
        boolean hasPurchased = user.getPurchasedBooks().contains(book);

        if (hasPurchased) {
            // TODO:  stream real file
            return ResponseEntity.ok("Access Granted: Here is your book: " + book.getBookFileName());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You must purchase this book to access the file.");
    }


    public ApiResponse getAllBooks() {
        return new ApiResponse("Books", bookRepository.findAll());
    }

    public ApiResponse getBookById(Long id){
        return new ApiResponse("Book", bookRepository.findById(id));
    }



    public ApiResponse updateBook(Long id, BookDto bookDto) {
        return bookRepository.findById(id).map(book -> {
            bookMapper.updateBookFromDto(bookDto, book);
            Book updatedBook = bookRepository.save(book);
            return new ApiResponse("updated book", updatedBook);
        }).orElseGet(() -> new ApiResponse().addError("id", "No book found with id = " + id ));
    }

    public ApiResponse deleteBookById(Long id){
        return bookRepository.findById(id).map(book -> {
            bookRepository.deleteById(id);
            return new ApiResponse("deleted book", book);
        }).orElseGet(() -> new ApiResponse().addError("id", "No book found with id = " + id ));

    }
}
