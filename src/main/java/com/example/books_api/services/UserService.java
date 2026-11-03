package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dto.book.BookResponseDto;
import com.example.books_api.entities.Purchase;
import com.example.books_api.exceptions.user.UserNotFoundException;
import com.example.books_api.mapper.BookMapper;
import com.example.books_api.dto.ApiResponse;
import com.example.books_api.entities.User;
import com.example.books_api.respsitories.PurchaseRepository;
import com.example.books_api.respsitories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;

    private final SecurityService securityService;

    @Autowired
    public UserService(PurchaseRepository purchaseRepository, UserRepository userRepository,
                       BookMapper bookMapper, SecurityService securityService) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.bookMapper = bookMapper;
        this.securityService = securityService;
    }

    // Signature unchanged, so UserController keeps working as is.
    @Transactional(readOnly = true)
    public ApiResponse getPurchasedBooks() {  // TODO: return DTOs
        // 1. Get current user
        String email = securityService.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Load the user's purchases (newest first) and map the books
        List<BookResponseDto> purchasedBooks = purchaseRepository
                .findByUserIdOrderByPurchasedAtDesc(user.getId())
                .stream()
                .map(Purchase::getBook)
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());

        return new ApiResponse(user.getFirstname() + "'s purchased books", purchasedBooks);
    }
}
