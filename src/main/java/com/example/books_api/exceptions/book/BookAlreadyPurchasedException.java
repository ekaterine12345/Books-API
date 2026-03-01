package com.example.books_api.exceptions.book;

public class BookAlreadyPurchasedException extends RuntimeException{
    public BookAlreadyPurchasedException(String message) {
        super(message);
    }
}
