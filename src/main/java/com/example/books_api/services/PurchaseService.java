package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dto.purchase.PurchaseResponseDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.Purchase;
import com.example.books_api.entities.User;
import com.example.books_api.exceptions.book.BookNotFoundException;
import com.example.books_api.exceptions.user.UserNotFoundException;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.PurchaseRepository;
import com.example.books_api.respsitories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository, BookRepository bookRepository,
                           UserRepository userRepository, SecurityService securityService) {
        this.purchaseRepository = purchaseRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    private User getCurrentUser() {  // helper method
        String email = securityService.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    /**
     * Buys a book for the current user. Idempotent: if the user already owns the book,
     * the existing purchase is returned (same behavior as the old many-to-many version).
     */
    @Transactional
    public PurchaseResponseDto purchaseBook(Long bookId) {
        User user = getCurrentUser();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        Purchase purchase = purchaseRepository.findByUserIdAndBookId(user.getId(), bookId)
                .orElseGet(() -> purchaseRepository.save(new Purchase(user, book)));

        return toDto(purchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getMyPurchases() {
        User user = getCurrentUser();
        return purchaseRepository.findByUserIdOrderByPurchasedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasPurchased(Long userId, Long bookId) {
        return purchaseRepository.existsByUserIdAndBookId(userId, bookId);
    }

    private PurchaseResponseDto toDto(Purchase purchase) {
        Book book = purchase.getBook();
        return PurchaseResponseDto.builder()
                .purchaseId(purchase.getId())
                .bookId(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .pricePaid(purchase.getPrice())
                .purchasedAt(purchase.getPurchasedAt())
                .build();
    }
}