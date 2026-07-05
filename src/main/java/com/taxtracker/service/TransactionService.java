package com.taxtracker.service;

import com.taxtracker.dto.CreateTransactionRequest;
import com.taxtracker.dto.TransactionDTO;
import com.taxtracker.dto.TransactionPageResponse;
import com.taxtracker.exception.TaxTrackerException;

import java.util.List;

public interface TransactionService {

    TransactionPageResponse getTransactions(String email, Integer pageNumber, Integer pageSize,
                                            String financialYear, String month, String organizationName,
                                            String type, Boolean allMonths)
            throws TaxTrackerException;

    List<TransactionDTO> getTransactionsForExport(String email, String financialYear) throws TaxTrackerException;

    TransactionDTO createTransaction(String email, CreateTransactionRequest request) throws TaxTrackerException;
}
