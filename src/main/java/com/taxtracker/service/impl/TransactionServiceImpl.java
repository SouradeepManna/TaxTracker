package com.taxtracker.service.impl;

import com.taxtracker.dto.CreateTransactionRequest;
import com.taxtracker.dto.TransactionDTO;
import com.taxtracker.dto.TransactionPageResponse;
import com.taxtracker.entity.TransactionEntity;
import com.taxtracker.exception.ResourceNotFoundException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.TransactionRepository;
import com.taxtracker.repository.TransactionSpecifications;
import com.taxtracker.service.TransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public TransactionPageResponse getTransactions(String email, Integer pageNumber, Integer pageSize,
                                                   String financialYear, String month, String organizationName,
                                                   String type, Boolean allMonths)
            throws TaxTrackerException {

        int page = (pageNumber == null || pageNumber < 1) ? 1 : pageNumber;
        int size = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("date").descending());

        // When allMonths is requested, ignore the month filter.
        String effectiveMonth = Boolean.TRUE.equals(allMonths) ? null : month;

        Specification<TransactionEntity> spec = TransactionSpecifications.withFilters(
                email, financialYear, effectiveMonth, organizationName, type);

        Page<TransactionEntity> result = transactionRepository.findAll(spec, pageable);

        if (result.isEmpty()) {
            throw new ResourceNotFoundException("app.message.transaction.not.found");
        }

        List<TransactionDTO> dtos = result.getContent().stream()
                .map(e -> modelMapper.map(e, TransactionDTO.class))
                .collect(Collectors.toList());

        TransactionPageResponse response = new TransactionPageResponse();
        response.setTotalRecords(result.getTotalElements());
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTransactions(dtos);
        return response;
    }

    @Override
    public List<TransactionDTO> getTransactionsForExport(String email, String financialYear) throws TaxTrackerException {
        List<TransactionEntity> entities = (financialYear != null && !financialYear.isBlank())
                ? transactionRepository.findByEmailAndFinancialYear(email, financialYear)
                : transactionRepository.findByEmail(email);

        if (entities.isEmpty()) {
            throw new ResourceNotFoundException("app.message.transaction.not.found");
        }

        return entities.stream()
                .map(e -> modelMapper.map(e, TransactionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDTO createTransaction(String email, CreateTransactionRequest request) throws TaxTrackerException {
        TransactionEntity entity = new TransactionEntity();
        entity.setEmail(email);
        entity.setDate(request.getDate());
        entity.setAmount(request.getAmount());
        entity.setTaxAmount(request.getTaxAmount());
        entity.setType(request.getType());
        entity.setOrganizationName(request.getOrganizationName());

        // Derive financial year from the date when not explicitly provided.
        String fy = (request.getFinancialYear() != null && !request.getFinancialYear().isBlank())
                ? request.getFinancialYear()
                : deriveFinancialYear(request.getDate());
        entity.setFinancialYear(fy);

        // Two-digit month string ("01".."12") used by the month filter and trends.
        entity.setMonth(deriveMonth(request.getDate()));

        TransactionEntity saved = transactionRepository.save(entity);
        return modelMapper.map(saved, TransactionDTO.class);
    }

    /**
     * Indian financial year runs April -> March. A date in Jan-Mar belongs to the
     * FY that started the previous calendar year.
     */
    private String deriveFinancialYear(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        if (month >= 4) {
            return year + "-" + (year + 1);
        }
        return (year - 1) + "-" + year;
    }

    private String deriveMonth(LocalDate date) {
        int m = date.getMonthValue();
        return m < 10 ? "0" + m : String.valueOf(m);
    }
}
