package com.taxtracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class Form90CResponse {
    private Long formId;
    private String email;
    private String name;
    private String mobileNumber;
    private String financialYear;
    private String status;
    private String documentName;
    private List<Form90CTransactionDTO> transactionHistory;
}
