package com.example.books_api.controllers;

import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/purchase")
    public ApiResponse purchaseFromCart() {

        return cartService.purchaseFromCart();
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ApiResponse getAllCart() {

        return cartService.getAllCart();
    }



    @PreAuthorize("hasRole('USER')")
    @PostMapping("/add/{bookId}")
    public ApiResponse addToCart(@PathVariable Long bookId) {

        return cartService.addToCart(bookId);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/delete/{bookId}")
    public ApiResponse removeFromCart(@PathVariable Long bookId) {

        return cartService.deleteFromCart(bookId);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/clear")
    public ApiResponse clearCart() {
        return cartService.clearCart();
    }
}

