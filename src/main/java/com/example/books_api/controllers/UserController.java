package com.example.books_api.controllers;

import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/my/books")
    public ApiResponse getPurchasedBooks(){
        return userService.getPurchasedBooks();

    }



}
