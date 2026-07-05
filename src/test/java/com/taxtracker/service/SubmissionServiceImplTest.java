package com.taxtracker.service;

import com.taxtracker.dto.ApiResponse;
import com.taxtracker.entity.Form90CEntity;
import com.taxtracker.entity.Form90CTransactionEntity;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.ResourceNotFoundException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.Form90CRepository;
import com.taxtracker.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock private Form90CRepository form90CRepository;
    @Mock private EmailService emailService;
    @InjectMocks private SubmissionServiceImpl submissionService;

    private Form90CEntity validForm() {
        Form90CEntity form = new Form90CEntity();
        form.setFormId(1L);
        form.setEmail("prajwal@taxtracker.com");
        form.setName("Prajwal Koppa");
        form.setFinancialYear("2024-2025");
        form.setStatus("DRAFT");
        form.setDocumentName("proof.pdf");
        List<Form90CTransactionEntity> rows = new ArrayList<>();
        rows.add(new Form90CTransactionEntity());
        form.setTransactionHistory(rows);
        return form;
    }

    @Test
    void submit_succeedsAndSendsEmail() throws TaxTrackerException {
        Form90CEntity form = validForm();
        when(form90CRepository.findById(1L)).thenReturn(Optional.of(form));

        ApiResponse res = submissionService.submit("prajwal@taxtracker.com", 1L);

        assertTrue(res.isSuccess());
        assertEquals("SUBMITTED", form.getStatus());
        verify(form90CRepository).save(form);
        verify(emailService).sendForm90CConfirmation("prajwal@taxtracker.com", "Prajwal Koppa", "2024-2025");
    }

    @Test
    void submit_throwsWhenFormNotFound() {
        when(form90CRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> submissionService.submit("prajwal@taxtracker.com", 99L));
        assertEquals("app.message.form.not.found", ex.getMessage());
        verifyNoInteractions(emailService);
    }

    @Test
    void submit_throwsWhenNoTransactions() {
        Form90CEntity form = validForm();
        form.setTransactionHistory(Collections.emptyList());
        when(form90CRepository.findById(1L)).thenReturn(Optional.of(form));

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> submissionService.submit("prajwal@taxtracker.com", 1L));
        assertEquals("app.message.no.transactions.added", ex.getMessage());
        verify(form90CRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void submit_throwsWhenNoDocument() {
        Form90CEntity form = validForm();
        form.setDocumentName(null);
        when(form90CRepository.findById(1L)).thenReturn(Optional.of(form));

        InvalidInputException ex = assertThrows(InvalidInputException.class,
                () -> submissionService.submit("prajwal@taxtracker.com", 1L));
        assertEquals("app.message.file.not.uploaded", ex.getMessage());
        verifyNoInteractions(emailService);
    }
}
