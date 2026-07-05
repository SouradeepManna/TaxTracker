package com.taxtracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Form90CRequest {

    @NotBlank(message = "Please provide a valid name")
    private String name;

    @NotBlank(message = "Please provide a valid mobileNumber")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "{mobile.invalid}")
    private String mobileNumber;

    private String financialYear;

    @Valid
    private List<Form90CTransactionDTO> transactionHistory = new ArrayList<>();
}
