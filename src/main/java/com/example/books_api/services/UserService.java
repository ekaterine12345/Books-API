package com.example.books_api.services;

import com.example.books_api.BookMapper;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.entities.User;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;

    @Autowired
    public UserService(BookRepository bookRepository, UserRepository userRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookMapper = bookMapper;
    }

    public ApiResponse getPurchasedBooks() {
        // 1. Get current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BookDto>  purchasedBooks = user.getPurchasedBooks()
                .stream().map(bookMapper::toDto).collect(Collectors.toList());
        return new ApiResponse(user.getFirstname() + "'s purchased books", purchasedBooks);
    }


//    public void purchaseBook(Long bookId) {

//
//        // 2. Get the book
//        Book book = bookRepository.findById(bookId)
//                .orElseThrow(() -> new RuntimeException("Book not found"));
//
//        // 3. Check if already purchased to avoid duplicates
//        if (!user.getPurchasedBooks().contains(book)) {
//            user.getPurchasedBooks().add(book);
//            userRepository.save(user); // Save the relationship in the join table
//        }
//    }




}
