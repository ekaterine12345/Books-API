package com.example.books_api.auth;

import com.example.books_api.config.JwtService;
import com.example.books_api.entities.User;
import com.example.books_api.respsitories.UserRepository;
import com.example.books_api.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstname("John");
        registerRequest.setLastname("Doe");
        registerRequest.setEmail("john@test.com");
        registerRequest.setPassword("password");

        authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("john@test.com");
        authenticationRequest.setPassword("password");
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn("jwtToken");

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());

        verify(repository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }


    @Test
    void shouldCreateCartAndAssignToUserOnRegister() {

        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtService.generateToken(any())).thenReturn("token");

        authenticationService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(Role.USER, savedUser.getRole());
        assertNotNull(savedUser.getCart());
        assertEquals(savedUser, savedUser.getCart().getUser());
    }


    @Test
    void shouldAuthenticateSuccessfully() {

        User user = User.builder()
                .email("john@test.com")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(repository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthenticationResponse response =
                authenticationService.authenticate(authenticationRequest);

        assertEquals("jwtToken", response.getToken());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        "john@test.com", "password"
                )
        );
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(repository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                authenticationService.authenticate(authenticationRequest)
        );
    }

}