package com.example.books_api.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
   User 1 ---- * Purchase * ---- 1 Book
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "purchases",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_purchase_user_book",
                columnNames = {"user_id", "book_id"}
        )
)
public class Purchase extends BaseEntity<Long> {
    @Id
    @Column(name = "purchase_id")
    @EqualsAndHashCode.Include
    @SequenceGenerator(name = "purchaseSeqId", sequenceName = "PURCHASE_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchaseSeqId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // price paid at the time of purchase (Book.price can change later)
    @Column(nullable = false)
    private Double price;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    public Purchase(User user, Book book) {
        this.user = user;
        this.book = book;
        this.price = book.getPrice();
        this.purchasedAt = LocalDateTime.now();
    }
}
