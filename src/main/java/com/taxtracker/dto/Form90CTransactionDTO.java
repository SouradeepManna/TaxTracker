package com.taxtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Form90CTransactionDTO {

    @NotBlank(message = "Please provide a valid organizationName")
    private String organizationName;

    @NotNull(message = "Please provide a valid amount")
    @Positive(message = "Please provide a valid amount")
    private BigDecimal amount;

    @NotNull(message = "Please provide a valid taxAmount")
    @Positive(message = "Please provide a valid taxAmount")
    private BigDecimal taxAmount;

    @NotBlank(message = "Please provide a valid type")
    private String type;
}
