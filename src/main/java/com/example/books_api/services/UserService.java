package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.mapper.BookMapper;
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

    private final SecurityService securityService;

    @Autowired
    public UserService(BookRepository bookRepository, UserRepository userRepository,
                       BookMapper bookMapper, SecurityService securityService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookMapper = bookMapper;
        this.securityService = securityService;
    }

    public ApiResponse getPurchasedBooks() {
        // 1. Get current user
        String email = securityService.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BookDto>  purchasedBooks = user.getPurchasedBooks()
                .stream().map(bookMapper::toDto).collect(Collectors.toList());
        return new ApiResponse(user.getFirstname() + "'s purchased books", purchasedBooks);
    }
}
