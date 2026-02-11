package com.example.books_api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "b7c443c9069b3aa26d0c53d7ca52b358bc18d460a354347c89083595d98c32b1";


    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){ // 24 hours valid token
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 3600 * 24))
                .signWith(getSingInKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

//    private Claims extractAllClaims(String token){
//        return Jwts.parser()
//                .verifyWith(getSingInKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            // Log the reason why the token failed if you want
            throw new RuntimeException("Invalid JWT token: " + e.getMessage());
        }
    }

    private SecretKey getSingInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}
