package com.example.books_api.controllers;


import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.User;
import com.example.books_api.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookPurchaseController {
    private final BookService bookService;


    @Autowired
    public BookPurchaseController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/{id}/purchase")
    public ApiResponse purchaseBook(@PathVariable Long id){
        return bookService.purchaseBook(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<String> downloadBook(@PathVariable Long id) {

        return bookService.downloadBook(id);
    }


    public ApiResponse getAllBooks(){
        return bookService.getAllBooks();
    }

}
