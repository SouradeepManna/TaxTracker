package com.taxtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsightsResponse {

    private String financialYear;
    private long totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal totalTax;

    // Tax split by type (TDS vs TCS, etc.)
    private List<TypeBreakdown> taxByType;

    // Top organizations by tax contribution
    private List<OrganizationBreakdown> topOrganizations;

    // Month-by-month tax totals (chronological)
    private List<MonthlyPoint> monthlyTrend;

    // Linear-regression projection of next month's tax
    private BigDecimal projectedNextMonthTax;
    private String trendNarrative;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeBreakdown {
        private String type;
        private BigDecimal taxAmount;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationBreakdown {
        private String organizationName;
        private BigDecimal taxAmount;
        private long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPoint {
        private String month;       // "01".."12"
        private BigDecimal taxAmount;
    }
}
