package com.taxtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressDTO {

    @NotBlank(message = "Please provide a valid addressLine1")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "Please provide a valid area")
    private String area;

    @NotBlank(message = "Please provide a valid city")
    private String city;

    @NotBlank(message = "Please provide a valid state")
    private String state;

    @NotBlank(message = "Please provide a valid pin")
    @Pattern(regexp = "^[0-9]{6}$", message = "{pin.invalid}")
    private String pin;
}
