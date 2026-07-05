package com.taxtracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "transaction_details")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "txn_date", nullable = false)
    private LocalDate date;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "organization_name", nullable = false, length = 150)
    private String organizationName;

    @Column(name = "financial_year", nullable = false, length = 9)
    private String financialYear;

    @Column(name = "txn_month", length = 2)
    private String month;
}
