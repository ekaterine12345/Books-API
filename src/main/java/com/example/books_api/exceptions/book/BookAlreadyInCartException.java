package com.example.books_api.exceptions.book;

public class BookAlreadyInCartException extends RuntimeException{
    public BookAlreadyInCartException(String message) {
        super(message);
    }
}
