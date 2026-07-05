package com.taxtracker.controller;

import com.taxtracker.dto.CreateTransactionRequest;
import com.taxtracker.dto.TransactionDTO;
import com.taxtracker.dto.TransactionPageResponse;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.exception.UnauthorizedException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthContext authContext;

    @GetMapping
    public ResponseEntity<TransactionPageResponse> getTransactions(
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String financialYear,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String organizationName,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean allMonths) throws TaxTrackerException {

        requireAuth();
        TransactionPageResponse response = transactionService.getTransactions(
                authContext.getEmail(), pageNumber, pageSize, financialYear, month,
                organizationName, type, allMonths);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) throws TaxTrackerException {
        requireAuth();
        TransactionDTO created = transactionService.createTransaction(authContext.getEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private void requireAuth() throws TaxTrackerException {
        if (!authContext.isAuthenticated()) {
            throw new UnauthorizedException("app.message.unauthorized.access");
        }
    }
}
