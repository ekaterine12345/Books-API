package com.example.books_api.respsitories;

import com.example.books_api.entities.Book;
import com.example.books_api.entities.Cart;
import com.example.books_api.entities.CartItem;
import com.example.books_api.entities.User;
import com.example.books_api.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;



    @Test
    @DisplayName("Should find cart by user email")
    void shouldFindCartByUserEmail() {

        // Create user
        User user = User.builder().firstname("Anna").lastname("Smith")
                .email("anna@test.com")
                .password("123")
                .role(Role.USER)
                .build();

        // Create cart
        Cart cart = Cart.builder().user(user).build();

        user.setCart(cart);

        userRepository.save(user); // Because of cascade my cart also saves

        Optional<Cart> found = cartRepository.findByUserEmail("anna@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail())
                .isEqualTo("anna@test.com");
    }

    @Test
    @DisplayName("Should save cart with items")
    void shouldSaveCartWithItems() {

        String user_email = "john@test.com";

        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .email(user_email)
                .password("123")
                .role(Role.USER)
                .build();

        Cart cart = Cart.builder()
                .user(user)
                .build();

        user.setCart(cart);

        // create book
        Book book = new Book();
        book.setTitle("Book Title 1");
        book.setAuthor("Author");
        book.setPrice(35.0);


        entityManager.persist(book);  // save book first, because I do not have cascade

        CartItem item = CartItem.builder()
                .cart(cart)
                .book(book)
                .build();

        cart.getItems().add(item);

        userRepository.save(user);

        Cart savedCart = cartRepository.findByUserEmail(user_email).get();

        assertThat(savedCart.getItems()).hasSize(1);
    }


    @Test
    @DisplayName("Should remove cart item when deleted")
    void shouldRemoveCartItem() {
        // when cart is cleared its items should be deleted

        // Given

        String user_email = "mike@test.com";

        User user = User.builder().firstname("Mike").lastname("Ross")
                .email(user_email).password("123")
                .role(Role.USER)
                .build();

        Cart cart = Cart.builder().user(user).build();

        user.setCart(cart);

        Book book = new Book();
        book.setTitle("Hibernate");
        book.setAuthor("Author");
        book.setPrice(25.0);

        entityManager.persist(book);

        CartItem item = CartItem.builder()
                .cart(cart)
                .book(book)
                .build();

        cart.getItems().add(item);

        userRepository.save(user);

        // Remove items in cart
        cart.getItems().clear();

        userRepository.save(user);

        Cart updated = cartRepository.findByUserEmail(user_email).get();

        assertThat(updated.getItems()).isEmpty();
    }



}