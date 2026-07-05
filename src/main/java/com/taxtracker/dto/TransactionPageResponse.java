package com.taxtracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransactionPageResponse {
    private long totalRecords;
    private int pageNumber;
    private int pageSize;
    private List<TransactionDTO> transactions;
}
