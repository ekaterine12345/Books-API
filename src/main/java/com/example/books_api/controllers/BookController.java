package com.example.books_api.controllers;

import com.example.books_api.BookMapper;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;


    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public ApiResponse getBookById(@PathVariable Long id){
        return bookService.getBookById(id);
    }


    @GetMapping
    public ApiResponse getAllBooks(){
        return bookService.getAllBooks();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse addBook(@RequestBody BookDto bookDto){
        return bookService.addBook(bookDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse updateBook(@PathVariable Long id, @RequestBody BookDto bookDto){
        return bookService.updateBook(id, bookDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse deleteBook(@PathVariable Long id){
        return  bookService.deleteBookById(id);
    }
}
