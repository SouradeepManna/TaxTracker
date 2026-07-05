package com.taxtracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Please provide a valid emailId")
    private String emailId;

    @NotBlank(message = "Please provide a valid password")
    private String password;
}
