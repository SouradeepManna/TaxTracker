package com.taxtracker.service;

import com.taxtracker.dto.CreateTransactionRequest;
import com.taxtracker.dto.TransactionDTO;
import com.taxtracker.dto.TransactionPageResponse;
import com.taxtracker.entity.TransactionEntity;
import com.taxtracker.exception.ResourceNotFoundException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.TransactionRepository;
import com.taxtracker.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private TransactionServiceImpl transactionService;

    @SuppressWarnings("unchecked")
    private void mockFindAll(Page<TransactionEntity> page) {
        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    }

    private TransactionEntity entity() {
        TransactionEntity t = new TransactionEntity();
        t.setId(1L);
        t.setEmail("prajwal@taxtracker.com");
        t.setDate(LocalDate.of(2024, 4, 10));
        t.setAmount(BigDecimal.valueOf(1000));
        t.setTaxAmount(BigDecimal.valueOf(100));
        t.setType("TDS");
        t.setOrganizationName("Infosys");
        t.setFinancialYear("2024-2025");
        return t;
    }

    @Test
    void getTransactions_allMonths_returnsPage() throws TaxTrackerException {
        mockFindAll(new PageImpl<>(List.of(entity())));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        TransactionPageResponse res = transactionService.getTransactions(
                "prajwal@taxtracker.com", 1, 10, null, null, null, null, true);

        assertEquals(1, res.getTotalRecords());
        assertEquals(1, res.getPageNumber());
        assertEquals(10, res.getPageSize());
        assertEquals(1, res.getTransactions().size());
    }

    @Test
    void getTransactions_byFinancialYear() throws TaxTrackerException {
        mockFindAll(new PageImpl<>(List.of(entity())));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        TransactionPageResponse res = transactionService.getTransactions(
                "prajwal@taxtracker.com", 1, 10, "2024-2025", null, null, null, false);

        assertEquals(1, res.getTotalRecords());
    }

    @Test
    void getTransactions_byAllFilters() throws TaxTrackerException {
        mockFindAll(new PageImpl<>(List.of(entity())));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        TransactionPageResponse res = transactionService.getTransactions(
                "prajwal@taxtracker.com", 1, 10, "2024-2025", "04", "Infosys", "TDS", false);

        assertEquals(1, res.getTotalRecords());
    }

    @Test
    void getTransactions_defaultsInvalidPageAndSize() throws TaxTrackerException {
        mockFindAll(new PageImpl<>(List.of(entity())));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        TransactionPageResponse res = transactionService.getTransactions(
                "prajwal@taxtracker.com", 0, 0, null, null, null, null, false);

        assertEquals(1, res.getPageNumber());
        assertEquals(10, res.getPageSize());
    }

    @Test
    void getTransactions_throwsWhenEmpty() {
        mockFindAll(new PageImpl<>(Collections.emptyList()));

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactions(
                "prajwal@taxtracker.com", 1, 10, null, null, null, null, false));
    }

    @Test
    void getTransactionsForExport_returnsList() throws TaxTrackerException {
        when(transactionRepository.findByEmail("prajwal@taxtracker.com")).thenReturn(List.of(entity()));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        List<TransactionDTO> res = transactionService.getTransactionsForExport("prajwal@taxtracker.com", null);

        assertEquals(1, res.size());
    }

    @Test
    void getTransactionsForExport_byFy_throwsWhenEmpty() {
        when(transactionRepository.findByEmailAndFinancialYear("prajwal@taxtracker.com", "2024-2025"))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionsForExport("prajwal@taxtracker.com", "2024-2025"));
    }

    @Test
    void createTransaction_savesAndDerivesFyAndMonth_aprilToMarch() throws TaxTrackerException {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setDate(LocalDate.of(2024, 4, 15));
        req.setAmount(BigDecimal.valueOf(5000));
        req.setTaxAmount(BigDecimal.valueOf(250));
        req.setType("TDS");
        req.setOrganizationName("Infosys");

        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        transactionService.createTransaction("prajwal@taxtracker.com", req);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        org.mockito.Mockito.verify(transactionRepository).save(captor.capture());
        TransactionEntity saved = captor.getValue();
        assertEquals("prajwal@taxtracker.com", saved.getEmail());
        assertEquals("2024-2025", saved.getFinancialYear()); // April -> FY starts this year
        assertEquals("04", saved.getMonth());
        assertEquals("Infosys", saved.getOrganizationName());
    }

    @Test
    void createTransaction_derivesPreviousFyForJanToMarch() throws TaxTrackerException {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setDate(LocalDate.of(2024, 2, 10));
        req.setAmount(BigDecimal.valueOf(5000));
        req.setTaxAmount(BigDecimal.valueOf(250));
        req.setType("TCS");
        req.setOrganizationName("TCS Solutions");

        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        transactionService.createTransaction("prajwal@taxtracker.com", req);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        org.mockito.Mockito.verify(transactionRepository).save(captor.capture());
        assertEquals("2023-2024", captor.getValue().getFinancialYear()); // Feb -> previous FY
        assertEquals("02", captor.getValue().getMonth());
    }

    @Test
    void createTransaction_respectsProvidedFinancialYear() throws TaxTrackerException {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setDate(LocalDate.of(2024, 4, 15));
        req.setAmount(BigDecimal.valueOf(5000));
        req.setTaxAmount(BigDecimal.valueOf(250));
        req.setType("TDS");
        req.setOrganizationName("Infosys");
        req.setFinancialYear("2099-2100");

        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(TransactionEntity.class), any())).thenReturn(new TransactionDTO());

        transactionService.createTransaction("prajwal@taxtracker.com", req);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        org.mockito.Mockito.verify(transactionRepository).save(captor.capture());
        assertEquals("2099-2100", captor.getValue().getFinancialYear());
    }
}
