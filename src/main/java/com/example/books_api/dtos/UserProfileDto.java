package com.example.books_api.dtos;

import com.example.books_api.dtos.book.BookDto;

import java.util.List;

public class UserProfileDto {
    private String username;
    private String email;
    private List<BookDto> purchasedBooks;

    // TODO: bank details
}
