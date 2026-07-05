package com.taxtracker.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

// Holds the authenticated user's email for the current request.
// Populated by JwtAuthFilter, read by controllers/services.
@Component
@RequestScope
public class AuthContext {

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAuthenticated() {
        return email != null;
    }
}
