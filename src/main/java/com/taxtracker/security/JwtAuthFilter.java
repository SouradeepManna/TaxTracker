package com.taxtracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Simple JWT filter: reads Bearer token, validates it, and stores the email
// in the request-scoped AuthContext. It does NOT block requests itself;
// protected controllers check AuthContext.isAuthenticated() and throw
// UnauthorizedException when needed. Public paths are skipped.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthContext authContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                authContext.setEmail(jwtUtil.getEmailFromToken(token));
            }
        }
        filterChain.doFilter(request, response);
    }
}
