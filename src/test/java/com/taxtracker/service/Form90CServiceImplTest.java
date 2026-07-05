package com.taxtracker.service;

import com.taxtracker.dto.Form90CRequest;
import com.taxtracker.dto.Form90CResponse;
import com.taxtracker.dto.Form90CTransactionDTO;
import com.taxtracker.entity.Form90CEntity;
import com.taxtracker.entity.Form90CTransactionEntity;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.ResourceNotFoundException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.Form90CRepository;
import com.taxtracker.service.impl.Form90CServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Form90CServiceImplTest {

    @Mock private Form90CRepository form90CRepository;
    @Mock private ModelMapper modelMapper;
    @InjectMocks private Form90CServiceImpl form90CService;

    private Form90CRequest request(boolean withTxn) {
        Form90CRequest req = new Form90CRequest();
        req.setName("Prajwal Koppa");
        req.setMobileNumber("9972649525");
        req.setFinancialYear("2024-2025");
        List<Form90CTransactionDTO> rows = new ArrayList<>();
        if (withTxn) {
            Form90CTransactionDTO dto = new Form90CTransactionDTO();
            dto.setOrganizationName("Infosys");
            dto.setAmount(BigDecimal.valueOf(1000));
            dto.setTaxAmount(BigDecimal.valueOf(100));
            dto.setType("TDS");
            rows.add(dto);
        }
        req.setTransactionHistory(rows);
        return req;
    }

    @Test
    void saveForm_draft_createsNewWhenNoExisting() throws TaxTrackerException {
        when(form90CRepository.findTopByEmailOrderByFormIdDesc("prajwal@taxtracker.com"))
                .thenReturn(Optional.empty());
        when(form90CRepository.save(any(Form90CEntity.class))).thenAnswer(i -> {
            Form90CEntity e = i.getArgument(0);
            e.setFormId(1L);
            return e;
        });

        Form90CResponse res = form90CService.saveForm("prajwal@taxtracker.com", request(true), "DRAFT");

        assertEquals("DRAFT", res.getStatus());
        assertEquals("Prajwal Koppa", res.getName());
        verify(form90CRepository).save(any(Form90CEntity.class));
    }

    @Test
    void saveForm_reusesExistingDraft() throws TaxTrackerException {
        Form90CEntity existing = new Form90CEntity();
        existing.setFormId(5L);
        existing.setStatus("DRAFT");
        existing.setTransactionHistory(new ArrayList<>());
        when(form90CRepository.findTopByEmailOrderByFormIdDesc("prajwal@taxtracker.com"))
                .thenReturn(Optional.of(existing));
        when(form90CRepository.save(any(Form90CEntity.class))).thenAnswer(i -> i.getArgument(0));

        Form90CResponse res = form90CService.saveForm("prajwal@taxtracker.com", request(true), "SUBMITTED");

        assertEquals("SUBMITTED", res.getStatus());
        assertEquals(5L, res.getFormId());
    }

    @Test
    void saveForm_throwsWhenNameMissing() {
        Form90CRequest req = request(true);
        req.setName("  ");
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> form90CService.saveForm("prajwal@taxtracker.com", req, "DRAFT"));
        assertEquals("app.message.form.incomplete", ex.getMessage());
    }

    @Test
    void saveForm_submitted_throwsWhenNoTransactions() {
        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> form90CService.saveForm("prajwal@taxtracker.com", request(false), "SUBMITTED"));
        assertEquals("app.message.no.transactions.added", ex.getMessage());
    }

    @Test
    void saveForm_throwsOnInvalidTransactionRow() {
        when(form90CRepository.findTopByEmailOrderByFormIdDesc("prajwal@taxtracker.com"))
                .thenReturn(Optional.empty());
        Form90CRequest req = request(false);
        Form90CTransactionDTO bad = new Form90CTransactionDTO();
        bad.setOrganizationName(""); // invalid
        bad.setAmount(BigDecimal.TEN);
        bad.setType("TDS");
        req.setTransactionHistory(Collections.singletonList(bad));

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> form90CService.saveForm("prajwal@taxtracker.com", req, "DRAFT"));
        assertEquals("app.message.invalid.format", ex.getMessage());
    }

    @Test
    void getForm_returnsMappedResponse() throws TaxTrackerException {
        Form90CEntity entity = new Form90CEntity();
        entity.setFormId(1L);
        entity.setEmail("prajwal@taxtracker.com");
        entity.setName("Prajwal Koppa");
        entity.setStatus("SUBMITTED");
        Form90CTransactionEntity row = new Form90CTransactionEntity();
        entity.setTransactionHistory(new ArrayList<>(List.of(row)));
        when(form90CRepository.findTopByEmailOrderByFormIdDesc("prajwal@taxtracker.com"))
                .thenReturn(Optional.of(entity));
        when(modelMapper.map(any(Form90CTransactionEntity.class), eq(Form90CTransactionDTO.class)))
                .thenReturn(new Form90CTransactionDTO());

        Form90CResponse res = form90CService.getForm("prajwal@taxtracker.com");

        assertEquals(1L, res.getFormId());
        assertEquals(1, res.getTransactionHistory().size());
    }

    @Test
    void getForm_throwsWhenNotFound() {
        when(form90CRepository.findTopByEmailOrderByFormIdDesc("prajwal@taxtracker.com"))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> form90CService.getForm("prajwal@taxtracker.com"));
    }
}
