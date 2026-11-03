package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dto.ApiResponse;
import com.example.books_api.dto.book.BookResponseDto;
import com.example.books_api.dto.cartItem.CartItemResponseDto;
import com.example.books_api.dto.CartResponseDto;
import com.example.books_api.entities.Book;
import com.example.books_api.entities.Cart;
import com.example.books_api.entities.CartItem;
import com.example.books_api.entities.Purchase;
import com.example.books_api.entities.User;
import com.example.books_api.exceptions.book.BookAlreadyInCartException;
import com.example.books_api.exceptions.book.BookAlreadyPurchasedException;
import com.example.books_api.exceptions.book.BookNotFoundException;
import com.example.books_api.exceptions.cart.CartNotFoundException;
import com.example.books_api.exceptions.cart.EmptyCartException;
import com.example.books_api.mapper.BookMapper;
import com.example.books_api.mapper.CartItemMapper;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.CartRepository;
import com.example.books_api.respsitories.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final PurchaseRepository purchaseRepository;
    private final BookMapper bookMapper;

    private final SecurityService securityService;
    private final CartItemMapper cartItemMapper;

    // Helper: Centralized logic to get the current user's cart
    private Cart getCartForCurrentUser() {
        String email = securityService.getCurrentUserEmail();
        return cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + email));
    }

    // Helper
    private Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
    }

    @Transactional
    public BookResponseDto addToCart(Long bookId) {  // TODO: return DTOs

        Cart cart = getCartForCurrentUser();
        Book book = getBookById(bookId);

        if (purchaseRepository.existsByUserIdAndBookId(cart.getUser().getId(), bookId)) {
            throw new BookAlreadyPurchasedException("that book is already purchased");
        }

        boolean alreadyInCart = cart.getItems().stream()
                .anyMatch(i -> i.getBook().getId().equals(bookId));

        if (alreadyInCart) {
            throw new BookAlreadyInCartException("Book already in cart");
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .book(book)
                .build();

        cart.getItems().add(item);
        cartRepository.save(cart);

        return bookMapper.toResponseDto(book);
    }

    @Transactional
    public ApiResponse deleteFromCart(Long bookId) {
        Cart cart = getCartForCurrentUser();

        boolean removed = cart.getItems().removeIf(i -> i.getBook().getId().equals(bookId));

        if (!removed) {
            throw new BookNotFoundException("Book with ID " + bookId + " was not found in your cart");
        }
        cartRepository.save(cart);
        return new ApiResponse("Book removed from cart with id", bookId);
    }

    @Transactional
    public ApiResponse clearCart() {

        Cart cart = getCartForCurrentUser();

        cart.getItems().clear();
        cartRepository.save(cart);

        return new ApiResponse("Cart cleared for user", securityService.getCurrentUserEmail());
    }

    @Transactional(readOnly = true)
    public CartResponseDto getAllCart() {
        Cart cart = getCartForCurrentUser();

        List<CartItemResponseDto> items = cart.getItems().stream()
                .map(cartItemMapper::toResponseDto).toList(); // TODO: create/use mapper, Done

        double totalPrice = items.stream().mapToDouble(CartItemResponseDto::getPrice).sum();

        return new CartResponseDto(cart.getId(), items, totalPrice);
        // return cartMapper.toResponseDto(getCartForCurrentUser());
    }

    @Transactional
    public ApiResponse purchaseFromCart(){

        Cart cart = getCartForCurrentUser();
        User user = cart.getUser();

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot purchase an empty cart.");
        }

        List<BookResponseDto> purchasedBooks = new ArrayList<>();
        List<Purchase> newPurchases = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()){
            Book book = cartItem.getBook();
            // skip books the user already owns (same behavior as before)
            if (!purchaseRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
                newPurchases.add(new Purchase(user, book)); // snapshots the current price
                purchasedBooks.add(bookMapper.toResponseDto(book));
            }
        }

        purchaseRepository.saveAll(newPurchases);
        cart.getItems().clear();
        cartRepository.save(cart);

        return new ApiResponse("Purchase success", purchasedBooks);
    }
}