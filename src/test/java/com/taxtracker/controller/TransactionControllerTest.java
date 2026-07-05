package com.taxtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxtracker.dto.CreateTransactionRequest;
import com.taxtracker.dto.TransactionDTO;
import com.taxtracker.dto.TransactionPageResponse;
import com.taxtracker.exception.GlobalExceptionHandler;
import com.taxtracker.exception.ResourceNotFoundException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.TransactionService;
import com.taxtracker.util.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import({GlobalExceptionHandler.class})
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TransactionService transactionService;
    @MockBean private AuthContext authContext;
    @MockBean private MessageResolver messageResolver;
    @MockBean private com.taxtracker.security.JwtUtil jwtUtil;

    @Test
    void getTransactions_returns200() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        TransactionPageResponse resp = new TransactionPageResponse();
        resp.setTotalRecords(1);
        resp.setPageNumber(1);
        resp.setPageSize(10);
        resp.setTransactions(new ArrayList<>());
        when(transactionService.getTransactions(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(resp);

        mockMvc.perform(get("/api/transactions")
                        .param("pageNumber", "1").param("pageSize", "10")
                        .param("organizationName", "Infosys").param("type", "TDS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1));
    }

    @Test
    void getTransactions_returns401WhenNotAuthenticated() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        when(messageResolver.resolve(any())).thenReturn("You are not authorized to access this resource.");

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTransactions_returns404WhenNoneFound() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        when(messageResolver.resolve(any())).thenReturn("No transactions found for the given criteria.");
        when(transactionService.getTransactions(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("app.message.transaction.not.found"));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_returns201() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getEmail()).thenReturn("prajwal@taxtracker.com");
        TransactionDTO dto = new TransactionDTO();
        dto.setId(10L);
        dto.setOrganizationName("Infosys");
        when(transactionService.createTransaction(eq("prajwal@taxtracker.com"), any(CreateTransactionRequest.class)))
                .thenReturn(dto);

        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setDate(LocalDate.of(2024, 4, 15));
        req.setAmount(BigDecimal.valueOf(5000));
        req.setTaxAmount(BigDecimal.valueOf(250));
        req.setType("TDS");
        req.setOrganizationName("Infosys");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.organizationName").value("Infosys"));
    }

    @Test
    void createTransaction_returns400OnInvalidType() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(true);
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setDate(LocalDate.of(2024, 4, 15));
        req.setAmount(BigDecimal.valueOf(5000));
        req.setTaxAmount(BigDecimal.valueOf(250));
        req.setType("XYZ"); // invalid, must be TDS/TCS
        req.setOrganizationName("Infosys");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_returns401WhenNotAuthenticated() throws Exception {
        when(authContext.isAuthenticated()).thenReturn(false);
        when(messageResolver.resolve(any())).thenReturn("You are not authorized to access this resource.");

        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setDate(LocalDate.of(2024, 4, 15));
        req.setAmount(BigDecimal.valueOf(5000));
        req.setTaxAmount(BigDecimal.valueOf(250));
        req.setType("TDS");
        req.setOrganizationName("Infosys");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
