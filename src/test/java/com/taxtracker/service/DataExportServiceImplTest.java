package com.taxtracker.service;

import com.taxtracker.dto.TransactionDTO;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.service.impl.DataExportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataExportServiceImplTest {

    @Mock private TransactionService transactionService;
    @InjectMocks private DataExportServiceImpl dataExportService;

    private TransactionDTO dto() {
        TransactionDTO t = new TransactionDTO();
        t.setId(1L);
        t.setDate(LocalDate.of(2024, 4, 10));
        t.setAmount(BigDecimal.valueOf(1000));
        t.setTaxAmount(BigDecimal.valueOf(100));
        t.setType("TDS");
        t.setOrganizationName("Infosys");
        t.setFinancialYear("2024-2025");
        return t;
    }

    @Test
    void export_jsonReturnsBytes() throws TaxTrackerException {
        when(transactionService.getTransactionsForExport("prajwal@taxtracker.com", null))
                .thenReturn(List.of(dto()));
        byte[] out = dataExportService.export("prajwal@taxtracker.com", "json", null);
        assertTrue(out.length > 0);
        assertTrue(new String(out).contains("Infosys"));
    }

    @Test
    void export_pdfReturnsBytes() throws TaxTrackerException {
        when(transactionService.getTransactionsForExport("prajwal@taxtracker.com", null))
                .thenReturn(List.of(dto()));
        byte[] out = dataExportService.export("prajwal@taxtracker.com", "pdf", null);
        assertTrue(out.length > 0);
        // PDF files start with %PDF
        assertEquals('%', (char) out[0]);
    }

    @Test
    void export_excelReturnsBytes() throws TaxTrackerException {
        when(transactionService.getTransactionsForExport("prajwal@taxtracker.com", null))
                .thenReturn(List.of(dto()));
        byte[] out = dataExportService.export("prajwal@taxtracker.com", "excel", null);
        assertTrue(out.length > 0);
    }

    @Test
    void export_defaultsToJsonWhenNullFormat() throws TaxTrackerException {
        when(transactionService.getTransactionsForExport("prajwal@taxtracker.com", null))
                .thenReturn(List.of(dto()));
        byte[] out = dataExportService.export("prajwal@taxtracker.com", null, null);
        assertTrue(out.length > 0);
    }

    @Test
    void export_throwsOnUnknownFormat() throws TaxTrackerException {
        when(transactionService.getTransactionsForExport("prajwal@taxtracker.com", null))
                .thenReturn(List.of(dto()));
        assertThrows(InvalidInputException.class,
                () -> dataExportService.export("prajwal@taxtracker.com", "xml", null));
    }

    @Test
    void contentType_returnsCorrectTypes() {
        assertEquals("application/pdf", dataExportService.contentType("pdf"));
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                dataExportService.contentType("excel"));
        assertEquals("application/json", dataExportService.contentType("json"));
        assertEquals("application/json", dataExportService.contentType(null));
    }

    @Test
    void fileName_returnsCorrectNames() {
        assertEquals("transactions.pdf", dataExportService.fileName("pdf"));
    }
}
