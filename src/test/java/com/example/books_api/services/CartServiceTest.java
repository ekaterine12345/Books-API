package com.example.books_api.services;

import com.example.books_api.config.SecurityService;
import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.dtos.book.BookResponseDto;
import com.example.books_api.dtos.CartResponseDto;
import com.example.books_api.dtos.cartItem.CartItemResponseDto;
import com.example.books_api.entities.*;
import com.example.books_api.exceptions.cart.CartNotFoundException;
import com.example.books_api.exceptions.book.BookAlreadyInCartException;
import com.example.books_api.exceptions.book.BookAlreadyPurchasedException;
import com.example.books_api.exceptions.cart.EmptyCartException;
import com.example.books_api.mapper.BookMapper;
import com.example.books_api.mapper.CartItemMapper;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private CartItemMapper cartItemMapper;

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
        // Given
        when(securityService.getCurrentUserEmail()).thenReturn(email);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        Book book = new Book();
        book.setId(1L);

        BookResponseDto savedDto = new BookResponseDto();

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toResponseDto(any(Book.class)))
                .thenReturn(savedDto);

        // When
        BookResponseDto response = cartService.addToCart(1L);

        // Then
        assertThat(response).isEqualTo(savedDto);
        assertEquals(1, cart.getItems().size());
        verify(cartRepository).save(cart);
    }


    @Test
    void shouldThrowWhenCartNotFound() {
        when(securityService.getCurrentUserEmail()).thenReturn(email);
        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class,
                () -> cartService.addToCart(1L));
    }

    @Test
    void shouldThrowWhenBookAlreadyInCart() {
        // Given
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


        // When, Then
        RuntimeException exception = assertThrows(
                BookAlreadyInCartException.class,
                () -> cartService.addToCart(1L)
        );
        assertEquals("Book already in cart", exception.getMessage());
    }

    @Test
    void shouldThrowWhenBookAlreadyPurchased() {
        // Given
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


        // When. Then
        RuntimeException exception = assertThrows(
                BookAlreadyPurchasedException.class,
                () -> cartService.addToCart(1L)
        );
        assertEquals("that book is already purchased", exception.getMessage());

    }

    // ========================= DELETE FROM CART =============================

    @Test
    void shouldDeleteFromCartSuccessfully() {
        // Given
        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book book = new Book();
        book.setId(1L);

        CartItem item = CartItem.builder().book(book).build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));

        // When
        ApiResponse response = cartService.deleteFromCart(1L);

        // Then
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
                CartNotFoundException.class,
                () -> cartService.deleteFromCart(1L)
        );

        assertEquals("Cart not found for user: " + email, exception.getMessage());

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
        // Given
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

        CartItemResponseDto itemDto = new CartItemResponseDto(1L, "Spring", "John", 20.0);

        when(cartRepository.findByUserEmail(email))
                .thenReturn(Optional.of(cart));

        when(cartItemMapper.toResponseDto(item)).thenReturn(itemDto);

        // When
        CartResponseDto responseDto = cartService.getAllCart();

        // Then
        assertEquals(10L, responseDto.getCartId());
        assertEquals(1, responseDto.getItems().size());
        assertEquals(20.0, responseDto.getTotalPrice());
    }


    // ========================= PURCHASE =============================

    @Test
    void shouldPurchaseFromCartSuccessfully() {
        // Given
        when(securityService.getCurrentUserEmail()).thenReturn(email);

        Book book = new Book();
        book.setId(1L);

        CartItem item = CartItem.builder().book(book).build();


        User user = new User();
        user.setId(1L);
        user.setPurchasedBooks(new ArrayList<>());


        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.getItems().add(item);
        cart.setUser(user);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));
        when(bookMapper.toResponseDto(any(Book.class)))
                .thenReturn(new BookResponseDto());

        // When
        ApiResponse response = cartService.purchaseFromCart();

        // Then
        assertEquals(1, user.getPurchasedBooks().size());
        assertTrue(cart.getItems().isEmpty());

        verify(userRepository).save(user);
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldThrowWhenCartEmpty() {
        // Given
        when(securityService.getCurrentUserEmail()).thenReturn(email);

        User user = new User();
        user.setPurchasedBooks(new ArrayList<>());


        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());
        cart.setUser(user);

        when(cartRepository.findByUserEmail(email)).thenReturn(Optional.of(cart));

        // When, Then
        assertThrows(EmptyCartException.class, () -> cartService.purchaseFromCart());
    }
}
