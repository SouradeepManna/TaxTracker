package com.taxtracker.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String addressLine1;
    private String addressLine2;
    private String area;
    private String city;
    private String state;
    private String pin;
    private String mobNumber;
}
