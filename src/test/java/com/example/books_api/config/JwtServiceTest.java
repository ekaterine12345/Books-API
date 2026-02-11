package com.example.books_api.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        userDetails = User.builder()
                .username("test@test.com")
                .password("password")
                .roles("USER")
                .build();
    }

    // ========================= GENERATE TOKEN =============================

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldGenerateTokenWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");

        String token = jwtService.generateToken(extraClaims, userDetails);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        assertEquals("USER", role);
    }

    // ========================= EXTRACT USERNAME =============================

    @Test
    void shouldExtractUsername() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("test@test.com", username);
    }

    // ========================= TOKEN VALIDATION =============================

    @Test
    void shouldReturnTrueWhenTokenIsValid() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = User.builder()
                .username("other@test.com")
                .password("password")
                .roles("USER")
                .build();

        boolean isValid = jwtService.isTokenValid(token, otherUser);

        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() throws InterruptedException {
        // Create token with short expiration manually
        JwtService shortLivedService = new JwtService() {
            @Override
            public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
                return io.jsonwebtoken.Jwts.builder()
                        .claims(extraClaims)
                        .subject(userDetails.getUsername())
                        .issuedAt(new java.util.Date(System.currentTimeMillis()))
                        .expiration(new java.util.Date(System.currentTimeMillis() + 1)) // expires almost immediately
                        .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                "b7c443c9069b3aa26d0c53d7ca52b358bc18d460a354347c89083595d98c32b1".getBytes()
                        ))
                        .compact();
            }
        };

        String token = shortLivedService.generateToken(userDetails);

        Thread.sleep(5); // wait to ensure expiration

        boolean isValid = shortLivedService.isTokenValid(token, userDetails);

        assertFalse(isValid);
    }

    // ========================= EXTRACT CLAIM =============================

    @Test
    void shouldExtractExpirationClaim() {
        String token = jwtService.generateToken(userDetails);

        var expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertNotNull(expiration);
        assertTrue(expiration.after(new java.util.Date()));
    }

    // ========================= INVALID TOKEN =============================

    @Test
    void shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.jwt.token";

        assertThrows(Exception.class, () ->
                jwtService.extractUsername(invalidToken)
        );
    }
}
