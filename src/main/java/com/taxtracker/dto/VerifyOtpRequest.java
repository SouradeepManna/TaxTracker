package com.taxtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Please provide a valid email")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(in|com)$", message = "{email.invalid}")
    private String email;

    @NotBlank(message = "Please provide a valid otp")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP should be exactly 6 digits")
    private String otp;
}
