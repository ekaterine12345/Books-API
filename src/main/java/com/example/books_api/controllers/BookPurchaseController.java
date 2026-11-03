package com.example.books_api.controllers;


import com.example.books_api.dto.ApiResponse;
import com.example.books_api.dto.FileDownloadDto;
import com.example.books_api.services.BookService;
import com.example.books_api.services.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/books")
public class BookPurchaseController {
    private final BookService bookService;
    private final PurchaseService purchaseService;

    @Autowired
    public BookPurchaseController(BookService bookService, PurchaseService purchaseService) {
        this.bookService = bookService;
        this.purchaseService = purchaseService;
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<ApiResponse> purchaseBook(@PathVariable Long id){
        return ResponseEntity.ok(new ApiResponse("book purchased ", purchaseService.purchaseBook(id)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadBook(@PathVariable Long id) throws MalformedURLException {
        FileDownloadDto fileDownloadDto = bookService.downloadBook(id);


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDownloadDto.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileDownloadDto.getFileName() + "\"")
                .body(fileDownloadDto.getResource());
    }
    public ApiResponse getAllBooks(){
        return new ApiResponse("books", bookService.getAllBooks());
    }

}