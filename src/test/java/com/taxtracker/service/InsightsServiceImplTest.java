package com.taxtracker.service;

import com.taxtracker.dto.InsightsResponse;
import com.taxtracker.entity.TransactionEntity;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.TransactionRepository;
import com.taxtracker.service.impl.InsightsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightsServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private InsightsServiceImpl insightsService;

    private TransactionEntity txn(String month, String type, String org, double amount, double tax) {
        TransactionEntity t = new TransactionEntity();
        t.setEmail("prajwal@taxtracker.com");
        t.setMonth(month);
        t.setDate(LocalDate.of(2024, Integer.parseInt(month), 10));
        t.setType(type);
        t.setOrganizationName(org);
        t.setAmount(BigDecimal.valueOf(amount));
        t.setTaxAmount(BigDecimal.valueOf(tax));
        t.setFinancialYear("2024-2025");
        return t;
    }

    @Test
    void getInsights_emptyReturnsZeros() throws TaxTrackerException {
        when(transactionRepository.findByEmail("prajwal@taxtracker.com")).thenReturn(Collections.emptyList());

        InsightsResponse res = insightsService.getInsights("prajwal@taxtracker.com", null);

        assertEquals(0, res.getTotalTransactions());
        assertEquals(BigDecimal.ZERO, res.getTotalTax());
        assertTrue(res.getTaxByType().isEmpty());
        assertTrue(res.getTopOrganizations().isEmpty());
        assertTrue(res.getMonthlyTrend().isEmpty());
        assertEquals(BigDecimal.ZERO, res.getProjectedNextMonthTax());
        assertNotNull(res.getTrendNarrative());
    }

    @Test
    void getInsights_aggregatesTotalsAndBreakdowns() throws TaxTrackerException {
        List<TransactionEntity> txns = Arrays.asList(
                txn("01", "TDS", "Infosys", 1000, 100),
                txn("01", "TCS", "TCS Solutions", 2000, 200),
                txn("02", "TDS", "Infosys", 1500, 150)
        );
        when(transactionRepository.findByEmailAndFinancialYear("prajwal@taxtracker.com", "2024-2025"))
                .thenReturn(txns);

        InsightsResponse res = insightsService.getInsights("prajwal@taxtracker.com", "2024-2025");

        assertEquals(3, res.getTotalTransactions());
        assertEquals(0, BigDecimal.valueOf(4500).compareTo(res.getTotalAmount()));
        assertEquals(0, BigDecimal.valueOf(450).compareTo(res.getTotalTax()));
        // TDS total tax = 250, TCS = 200 -> TDS first
        assertEquals("TDS", res.getTaxByType().get(0).getType());
        assertEquals(0, BigDecimal.valueOf(250).compareTo(res.getTaxByType().get(0).getTaxAmount()));
        // Infosys top org (100+150=250 > 200)
        assertEquals("Infosys", res.getTopOrganizations().get(0).getOrganizationName());
        // two months
        assertEquals(2, res.getMonthlyTrend().size());
        assertEquals("01", res.getMonthlyTrend().get(0).getMonth());
    }

    @Test
    void getInsights_projectsNextMonthWithLinearRegression() throws TaxTrackerException {
        // Perfectly linear increasing tax: 100, 200, 300 -> next should be ~400
        List<TransactionEntity> txns = Arrays.asList(
                txn("01", "TDS", "Infosys", 1000, 100),
                txn("02", "TDS", "Infosys", 1000, 200),
                txn("03", "TDS", "Infosys", 1000, 300)
        );
        when(transactionRepository.findByEmail("prajwal@taxtracker.com")).thenReturn(txns);

        InsightsResponse res = insightsService.getInsights("prajwal@taxtracker.com", null);

        assertEquals(0, BigDecimal.valueOf(400.00).compareTo(res.getProjectedNextMonthTax()));
        assertTrue(res.getTrendNarrative().contains("Projected tax next month"));
        assertTrue(res.getTrendNarrative().toLowerCase().contains("upward"));
    }

    @Test
    void getInsights_singleMonthProjectionEqualsThatMonth() throws TaxTrackerException {
        List<TransactionEntity> txns = Collections.singletonList(txn("05", "TDS", "Infosys", 1000, 500));
        when(transactionRepository.findByEmail("prajwal@taxtracker.com")).thenReturn(txns);

        InsightsResponse res = insightsService.getInsights("prajwal@taxtracker.com", null);

        assertEquals(0, BigDecimal.valueOf(500).compareTo(res.getProjectedNextMonthTax()));
        assertEquals(1, res.getMonthlyTrend().size());
    }

    @Test
    void getInsights_handlesNullTaxAmountsAndMonthFromDate() throws TaxTrackerException {
        TransactionEntity t = new TransactionEntity();
        t.setEmail("prajwal@taxtracker.com");
        t.setMonth(null); // force derive-from-date
        t.setDate(LocalDate.of(2024, 7, 15));
        t.setType("TDS");
        t.setOrganizationName("Infosys");
        t.setAmount(BigDecimal.valueOf(1000));
        t.setTaxAmount(null); // null tax must be treated as zero
        t.setFinancialYear("2024-2025");

        when(transactionRepository.findByEmail("prajwal@taxtracker.com"))
                .thenReturn(new ArrayList<>(Collections.singletonList(t)));

        InsightsResponse res = insightsService.getInsights("prajwal@taxtracker.com", null);

        assertEquals(1, res.getTotalTransactions());
        assertEquals("07", res.getMonthlyTrend().get(0).getMonth());
        assertEquals(0, BigDecimal.ZERO.compareTo(res.getMonthlyTrend().get(0).getTaxAmount()));
    }
}
