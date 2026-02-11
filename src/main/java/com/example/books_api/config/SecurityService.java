package com.example.books_api.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    public String getCurrentUserEmail() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}

