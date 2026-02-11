package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.CartResponseDto;
import com.example.books_api.entities.*;
import com.example.books_api.respsitories.BookRepository;
import com.example.books_api.respsitories.CartRepository;
import com.example.books_api.respsitories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private CartService cartService;

    private final String email = "test@test.com";

    // ========================= ADD TO CART =============================

    @Test
    void shouldAddBookToCartSuccessfully() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        Book book = new Book();
        book.setId(1L);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        ApiResponse response = cartService.addToCart(1L);

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey("Book added to cart"));
        assertEquals(1, cart.getItems().size());

        verify(cartRepository).save(cart);
    }


    @Test
    void shouldThrowWhenCartNotFound() {
        when(securityService.getCurrentUserEmail()).thenReturn(email);
        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> cartService.addToCart(1L));
    }

    @Test
    void shouldThrowWhenBookAlreadyInCart() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book book = new Book();
        book.setId(1L);

        CartItem item = CartItem.builder().book(book).build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());
        cart.setUser(user);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));



        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(1L)
        );
        assertEquals("Book already in cart", exception.getMessage());
    }

    @Test
    void shouldThrowWhenBookAlreadyPurchased() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book book = new Book();
        book.setId(1L);

        CartItem item = CartItem.builder().book(book).build();


        User user = new User();
        user.setPurchasedBooks(new ArrayList<>(List.of(book)));

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setUser(user);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));



        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(1L)
        );
        assertEquals("that book is already purchased", exception.getMessage());

    }

    // ========================= DELETE FROM CART =============================

    @Test
    void shouldDeleteFromCartSuccessfully() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book book = new Book();
        book.setId(1L);

        CartItem item = CartItem.builder().book(book).build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));

        ApiResponse response = cartService.deleteFromCart(1L);

        assertEquals(0, cart.getItems().size());
        assertTrue(response.getData().containsKey("Book removed from cart with id"));
        verify(cartRepository).save(cart);
    }


    @Test
    void shouldThrowExceptionWhenItemNotInCart() {
        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book bookInCart = new Book();
        bookInCart.setId(99L); // ID is different from the one I want to delete

        CartItem item = CartItem.builder().book(bookInCart).build();
        Cart cart = new Cart();

        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.deleteFromCart(1L); // Trying to delete ID 1, but only 99 exists
        });

        assertEquals("Book with ID 1 was not found in your cart", exception.getMessage());
        // Verify save was NEVER called because logic stopped at the exception
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldThrowWhenCartNotFoundWhileDeleting() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);
        when(cartRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.deleteFromCart(1L)
        );

        assertEquals("Cart not found", exception.getMessage());

        verify(cartRepository, never()).save(any());
    }


    // ========================= CLEAR CART =============================

    @Test
    void shouldClearCartSuccessfully() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(new CartItem());

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));

        ApiResponse response = cartService.clearCart();

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    // ========================= GET ALL CART =============================

    @Test
    void shouldReturnAllCartItems() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Spring");
        book.setAuthor("John");
        book.setPrice(20.0);

        CartItem item = CartItem.builder()
                .book(book)
                .build();

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findByUserEmail(email))
                .thenReturn(Optional.of(cart));

        ApiResponse response = cartService.getAllCart();

        assertTrue(response.getError().isEmpty());
        assertTrue(response.getData().containsKey("cart"));

        CartResponseDto dto =
                (CartResponseDto) response.getData().get("cart");

        assertEquals(10L, dto.getCartId());
        assertEquals(1, dto.getItems().size());
        assertEquals(20.0, dto.getTotalPrice());
    }


    // ========================= PURCHASE =============================

    @Test
    void shouldPurchaseFromCartSuccessfully() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book book = new Book();
        book.setId(1L);

        CartItem item = CartItem.builder().book(book).build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));

        ApiResponse response = cartService.purchaseFromCart();

        assertEquals(1, user.getPurchasedBooks().size());
        assertTrue(cart.getItems().isEmpty());

        verify(userRepository).save(user);
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowWhenCartEmpty() {

        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class,
                () -> cartService.purchaseFromCart());
    }
}

