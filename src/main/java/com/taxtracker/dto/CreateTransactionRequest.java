package com.taxtracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "Please provide a valid date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Please provide a valid amount")
    @PositiveOrZero(message = "Amount must be zero or positive")
    private BigDecimal amount;

    @NotNull(message = "Please provide a valid taxAmount")
    @PositiveOrZero(message = "Tax amount must be zero or positive")
    private BigDecimal taxAmount;

    @NotBlank(message = "Please provide a valid type")
    @Pattern(regexp = "^(TDS|TCS)$", message = "Type must be either TDS or TCS")
    private String type;

    @NotBlank(message = "Please provide a valid organizationName")
    private String organizationName;

    // Optional: if omitted, derived from the date (Indian FY: Apr-Mar)
    private String financialYear;
}
