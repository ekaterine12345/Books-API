package com.example.books_api.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PurchaseResponseDto {
    private Long purchaseId;
    private Long bookId;
    private String title;
    private String author;
    private Double pricePaid;
    private LocalDateTime purchasedAt;
}
