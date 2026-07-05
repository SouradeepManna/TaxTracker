package com.taxtracker.service.impl;

import com.taxtracker.dto.InsightsResponse;
import com.taxtracker.entity.TransactionEntity;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.TransactionRepository;
import com.taxtracker.service.InsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class InsightsServiceImpl implements InsightsService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public InsightsResponse getInsights(String email, String financialYear) throws TaxTrackerException {
        List<TransactionEntity> txns = (financialYear == null || financialYear.isBlank())
                ? transactionRepository.findByEmail(email)
                : transactionRepository.findByEmailAndFinancialYear(email, financialYear);

        InsightsResponse response = new InsightsResponse();
        response.setFinancialYear(financialYear);

        if (txns == null || txns.isEmpty()) {
            response.setTotalTransactions(0);
            response.setTotalAmount(BigDecimal.ZERO);
            response.setTotalTax(BigDecimal.ZERO);
            response.setTaxByType(new ArrayList<>());
            response.setTopOrganizations(new ArrayList<>());
            response.setMonthlyTrend(new ArrayList<>());
            response.setProjectedNextMonthTax(BigDecimal.ZERO);
            response.setTrendNarrative("No transactions available to analyze yet.");
            return response;
        }

        BigDecimal totalAmount = txns.stream()
                .map(TransactionEntity::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = txns.stream()
                .map(TransactionEntity::getTaxAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalTransactions(txns.size());
        response.setTotalAmount(totalAmount);
        response.setTotalTax(totalTax);
        response.setTaxByType(buildTypeBreakdown(txns));
        response.setTopOrganizations(buildTopOrganizations(txns));

        List<InsightsResponse.MonthlyPoint> trend = buildMonthlyTrend(txns);
        response.setMonthlyTrend(trend);

        BigDecimal projection = projectNextMonth(trend);
        response.setProjectedNextMonthTax(projection);
        response.setTrendNarrative(buildNarrative(trend, projection));

        return response;
    }

    private List<InsightsResponse.TypeBreakdown> buildTypeBreakdown(List<TransactionEntity> txns) {
        Map<String, BigDecimal> taxByType = new LinkedHashMap<>();
        Map<String, Long> countByType = new LinkedHashMap<>();
        for (TransactionEntity t : txns) {
            String type = t.getType() == null ? "UNKNOWN" : t.getType();
            taxByType.merge(type, nz(t.getTaxAmount()), BigDecimal::add);
            countByType.merge(type, 1L, Long::sum);
        }
        List<InsightsResponse.TypeBreakdown> result = new ArrayList<>();
        for (String type : taxByType.keySet()) {
            result.add(new InsightsResponse.TypeBreakdown(type, taxByType.get(type), countByType.get(type)));
        }
        result.sort(Comparator.comparing(InsightsResponse.TypeBreakdown::getTaxAmount).reversed());
        return result;
    }

    private List<InsightsResponse.OrganizationBreakdown> buildTopOrganizations(List<TransactionEntity> txns) {
        Map<String, BigDecimal> taxByOrg = new LinkedHashMap<>();
        Map<String, Long> countByOrg = new LinkedHashMap<>();
        for (TransactionEntity t : txns) {
            String org = t.getOrganizationName() == null ? "UNKNOWN" : t.getOrganizationName();
            taxByOrg.merge(org, nz(t.getTaxAmount()), BigDecimal::add);
            countByOrg.merge(org, 1L, Long::sum);
        }
        return taxByOrg.entrySet().stream()
                .map(e -> new InsightsResponse.OrganizationBreakdown(e.getKey(), e.getValue(), countByOrg.get(e.getKey())))
                .sorted(Comparator.comparing(InsightsResponse.OrganizationBreakdown::getTaxAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<InsightsResponse.MonthlyPoint> buildMonthlyTrend(List<TransactionEntity> txns) {
        // Aggregate tax by month; TreeMap keeps "01".."12" in order.
        Map<String, BigDecimal> byMonth = new TreeMap<>();
        for (TransactionEntity t : txns) {
            String month = deriveMonth(t);
            byMonth.merge(month, nz(t.getTaxAmount()), BigDecimal::add);
        }
        List<InsightsResponse.MonthlyPoint> trend = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : byMonth.entrySet()) {
            trend.add(new InsightsResponse.MonthlyPoint(e.getKey(), e.getValue()));
        }
        return trend;
    }

    private String deriveMonth(TransactionEntity t) {
        if (t.getMonth() != null && !t.getMonth().isBlank()) {
            return t.getMonth().length() == 1 ? "0" + t.getMonth() : t.getMonth();
        }
        if (t.getDate() != null) {
            int m = t.getDate().getMonthValue();
            return m < 10 ? "0" + m : String.valueOf(m);
        }
        return "00";
    }

    /**
     * Linear regression (least squares) over monthly tax totals, projecting the
     * next month. x = 1..n (month index), y = tax total. Returns max(0, prediction).
     */
    private BigDecimal projectNextMonth(List<InsightsResponse.MonthlyPoint> trend) {
        int n = trend.size();
        if (n == 0) return BigDecimal.ZERO;
        if (n == 1) return trend.get(0).getTaxAmount();

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = trend.get(i).getTaxAmount().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double denom = (n * sumX2) - (sumX * sumX);
        if (denom == 0) {
            return BigDecimal.valueOf(sumY / n).setScale(2, RoundingMode.HALF_UP);
        }
        double slope = ((n * sumXY) - (sumX * sumY)) / denom;
        double intercept = (sumY - (slope * sumX)) / n;
        double nextX = n + 1;
        double prediction = intercept + (slope * nextX);
        if (prediction < 0) prediction = 0;
        return BigDecimal.valueOf(prediction).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildNarrative(List<InsightsResponse.MonthlyPoint> trend, BigDecimal projection) {
        if (trend.size() < 2) {
            return "Not enough monthly data yet to establish a clear trend.";
        }
        BigDecimal first = trend.get(0).getTaxAmount();
        BigDecimal last = trend.get(trend.size() - 1).getTaxAmount();
        int cmp = last.compareTo(first);
        String direction;
        if (cmp > 0) {
            direction = "Your tax outgo is trending upward.";
        } else if (cmp < 0) {
            direction = "Your tax outgo is trending downward.";
        } else {
            direction = "Your tax outgo is holding steady.";
        }
        return direction + " Projected tax next month: " + projection + " (linear regression on your monthly totals).";
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
