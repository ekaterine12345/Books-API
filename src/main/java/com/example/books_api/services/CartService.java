package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.CartItemResponseDto;
import com.example.books_api.dtos.CartResponseDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.Cart;
import com.example.books_api.entities.CartItem;
import com.example.books_api.entities.User;
import com.example.books_api.exceptions.BookNotFoundException;
import com.example.books_api.exceptions.UserNotFoundException;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.CartRepository;
import com.example.books_api.respsitories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private final SecurityService securityService;


    public ApiResponse addToCart(Long bookId) {  // TODO: return DTOs
        // 1. Get current user
        String email = securityService.getCurrentUserEmail();   // SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Cart was not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book was not found"));

        boolean alreadyPurchased = cart.getUser().getPurchasedBooks().contains(book);

        if (alreadyPurchased) {
            throw new RuntimeException("that book is already purchased");
        }

        boolean alreadyInCart = cart.getItems().stream()
                        .anyMatch(i -> i.getBook().getId().equals(bookId));

        if (alreadyInCart) {
            throw new RuntimeException("Book already in cart");
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .book(book)
                .build();

        cart.getItems().add(item);
        cartRepository.save(cart);

        return new ApiResponse("Book added to cart", book);
    }

    public ApiResponse deleteFromCart(Long bookId) {

        String email = securityService.getCurrentUserEmail();
        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        boolean removed = cart.getItems().removeIf(i -> i.getBook().getId().equals(bookId));


        if (!removed) {
            throw new RuntimeException("Book with ID " + bookId + " was not found in your cart");
        }
        cartRepository.save(cart);
        return new ApiResponse("Book removed from cart with id", bookId);
    }

    public ApiResponse clearCart() {
        String email = securityService.getCurrentUserEmail();
        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);

        return new ApiResponse("Cart cleared for user", email);
    }

    public ApiResponse getAllCart() {
        String email = securityService.getCurrentUserEmail();
        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItemResponseDto> items = cart.getItems().stream()
                .map(item  -> new CartItemResponseDto(item.getBook().getId(),
                        item.getBook().getTitle(),
                        item.getBook().getAuthor(),
                        item.getBook().getPrice())).collect(Collectors.toList());

        double totalPrice = items.stream().mapToDouble(CartItemResponseDto::getPrice).sum();

        CartResponseDto response = new CartResponseDto(cart.getId(), items, totalPrice);

        return new ApiResponse("cart", response);
    }

    @Transactional
    public ApiResponse purchaseFromCart(){
        String email = securityService.getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart was not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (CartItem cartItem : cart.getItems()){
            Book book = cartItem.getBook();
            if (!user.getPurchasedBooks().contains(book)) {
                user.getPurchasedBooks().add(book);
            }
        }

        cart.getItems().clear();
        userRepository.save(user);
        cartRepository.save(cart);

        return new ApiResponse("Purchase success", user.getPurchasedBooks());
    }
}
