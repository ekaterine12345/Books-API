package com.example.books_api.exceptions;

public class BookAccessDeniedException extends RuntimeException{
    public BookAccessDeniedException(String message) {
        super(message);
    }
}
