package com.taxtracker.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String name;
    private String email;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(String token, String name, String email, String message) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.message = message;
    }
}
