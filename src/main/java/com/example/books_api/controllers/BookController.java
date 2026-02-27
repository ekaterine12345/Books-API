package com.example.books_api.controllers;

import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.BookDto;
import com.example.books_api.dtos.UpdateBookDto;
import com.example.books_api.services.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
        return new ApiResponse("Book", bookService.getBook(id));
    }


    @GetMapping
    public ApiResponse getAllBooks(){
        return new ApiResponse("Books", bookService.getAllBooks());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse addBook(@Valid @RequestBody BookDto bookDto){
        return  new ApiResponse("New Book", bookService.addBook(bookDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/file")
    public ApiResponse uploadOrReplaceBookFile(@PathVariable Long id, // bookId
                                               @RequestPart("book_file") MultipartFile multipartFile) throws IOException {
      return new ApiResponse("book", bookService.updateBookFile(id, multipartFile));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse updateBook(@PathVariable Long id,
                                  @Valid @RequestBody UpdateBookDto bookDto){
        return new ApiResponse("updated book", bookService.updateBook(id, bookDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse deleteBook(@PathVariable Long id){
        return new ApiResponse("deleted book", bookService.deleteBookById(id));
    }
}
