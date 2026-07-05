package com.taxtracker.repository;

import com.taxtracker.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository
        extends JpaRepository<TransactionEntity, Long>, JpaSpecificationExecutor<TransactionEntity> {

    Page<TransactionEntity> findByEmail(String email, Pageable pageable);

    Page<TransactionEntity> findByEmailAndFinancialYear(String email, String financialYear, Pageable pageable);

    Page<TransactionEntity> findByEmailAndFinancialYearAndMonth(String email, String financialYear, String month, Pageable pageable);

    List<TransactionEntity> findByEmail(String email);

    List<TransactionEntity> findByEmailAndFinancialYear(String email, String financialYear);
}
