package com.taxtracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    // First Middle Last - alphabets only, single space between words
    @NotBlank(message = "Please provide a valid name")
    @Pattern(regexp = "^[A-Za-z]+( [A-Za-z]+)*$", message = "{name.invalid}")
    private String name;

    @NotBlank(message = "Please provide a valid email")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(in|com)$", message = "{email.invalid}")
    private String email;

    // >=1 uppercase, >=1 lowercase, >=1 digit, >=1 special, 7-20 chars
    @NotBlank(message = "Please provide a valid password")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{7,20}$", message = "{password.invalid}")
    private String password;

    @NotBlank(message = "Please provide a valid mobNumber")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "{mobile.invalid}")
    private String mobNumber;

    @Valid
    private AddressDTO address;
}
