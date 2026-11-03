package com.example.books_api.respsitories;

import com.example.books_api.entities.Purchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    Optional<Purchase> findByUserIdAndBookId(Long userId, Long bookId);

    // fetch the book together with the purchase to avoid N+1 queries
    @EntityGraph(attributePaths = "book")
    List<Purchase> findByUserIdOrderByPurchasedAtDesc(Long userId);

    boolean existsByBookId(Long bookId);
}