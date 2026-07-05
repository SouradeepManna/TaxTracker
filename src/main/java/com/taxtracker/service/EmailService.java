package com.taxtracker.service;

public interface EmailService {

    void sendOtpEmail(String toEmail, String name, String otp);

    void sendRegistrationConfirmation(String toEmail, String name);

    void sendForm90CConfirmation(String toEmail, String name, String financialYear);
}
