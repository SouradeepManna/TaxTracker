package com.taxtracker.service;

import com.taxtracker.dto.LoginRequest;
import com.taxtracker.dto.LoginResponse;
import com.taxtracker.dto.RegisterRequest;
import com.taxtracker.dto.VerifyOtpRequest;
import com.taxtracker.exception.TaxTrackerException;

public interface AuthService {

    /**
     * Step 1 of registration: validate, store a pending registration and email an OTP.
     * No real account is created yet.
     */
    String initiateRegistration(RegisterRequest request) throws TaxTrackerException;

    /**
     * Step 2 of registration: verify the OTP, create the real account and email a
     * confirmation message.
     */
    String verifyOtpAndRegister(VerifyOtpRequest request) throws TaxTrackerException;

    LoginResponse login(LoginRequest request) throws TaxTrackerException;
}
