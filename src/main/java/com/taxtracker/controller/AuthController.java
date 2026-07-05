package com.taxtracker.controller;

import com.taxtracker.dto.ApiResponse;
import com.taxtracker.dto.LoginRequest;
import com.taxtracker.dto.LoginResponse;
import com.taxtracker.dto.RegisterRequest;
import com.taxtracker.dto.VerifyOtpRequest;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Step 1: accept registration details, send an OTP to the user's email.
     * Returns 200 (not 201) because no account is created yet.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request)
            throws TaxTrackerException {
        String message = authService.initiateRegistration(request);
        return ResponseEntity.ok(new ApiResponse(message, true));
    }

    /**
     * Step 2: verify the OTP and create the account. Returns 201 on success.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request)
            throws TaxTrackerException {
        String message = authService.verifyOtpAndRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(message, true));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request)
            throws TaxTrackerException {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        return ResponseEntity.ok(new ApiResponse("Logged out successfully", true));
    }
}
