package com.example.books_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemResponseDto {
    private Long bookId;
    private String title;
    private String author;
    private Double price;
}

