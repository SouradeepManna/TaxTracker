package com.taxtracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "form_90c")
public class Form90CEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "form_id")
    private Long formId;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "mobile_number", nullable = false, length = 10)
    private String mobileNumber;

    @Column(name = "financial_year", length = 9)
    private String financialYear;

    // DRAFT (saved for later) or SUBMITTED
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "document_name", length = 255)
    private String documentName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Form90CTransactionEntity> transactionHistory = new ArrayList<>();
}
