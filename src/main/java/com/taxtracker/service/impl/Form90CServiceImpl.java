package com.taxtracker.service.impl;

import com.taxtracker.dto.Form90CRequest;
import com.taxtracker.dto.Form90CResponse;
import com.taxtracker.dto.Form90CTransactionDTO;
import com.taxtracker.entity.Form90CEntity;
import com.taxtracker.entity.Form90CTransactionEntity;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.ResourceNotFoundException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.Form90CRepository;
import com.taxtracker.service.Form90CService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Form90CServiceImpl implements Form90CService {

    @Autowired
    private Form90CRepository form90CRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Form90CResponse saveForm(String email, Form90CRequest request, String status) throws TaxTrackerException {
        if (request.getName() == null || request.getName().isBlank()
                || request.getMobileNumber() == null || request.getMobileNumber().isBlank()) {
            throw new InvalidInputException("app.message.form.incomplete");
        }

        // A SUBMITTED form must carry at least one transaction row
        if ("SUBMITTED".equalsIgnoreCase(status)
                && (request.getTransactionHistory() == null || request.getTransactionHistory().isEmpty())) {
            throw new InvalidInputException("app.message.no.transactions.added");
        }

        // Reuse latest draft for this email if present, else new
        Form90CEntity entity = form90CRepository.findTopByEmailOrderByFormIdDesc(email)
                .filter(f -> "DRAFT".equalsIgnoreCase(f.getStatus()))
                .orElseGet(Form90CEntity::new);

        entity.setEmail(email);
        entity.setName(request.getName());
        entity.setMobileNumber(request.getMobileNumber());
        entity.setFinancialYear(request.getFinancialYear());
        entity.setStatus(status == null ? "DRAFT" : status.toUpperCase());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        entity.getTransactionHistory().clear();
        if (request.getTransactionHistory() != null) {
            for (Form90CTransactionDTO dto : request.getTransactionHistory()) {
                if (dto.getOrganizationName() == null || dto.getOrganizationName().isBlank()
                        || dto.getAmount() == null || dto.getType() == null || dto.getType().isBlank()) {
                    throw new InvalidInputException("app.message.invalid.format");
                }
                Form90CTransactionEntity row = new Form90CTransactionEntity();
                row.setOrganizationName(dto.getOrganizationName());
                row.setAmount(dto.getAmount());
                row.setTaxAmount(dto.getTaxAmount());
                row.setType(dto.getType());
                row.setForm(entity);
                entity.getTransactionHistory().add(row);
            }
        }

        Form90CEntity saved = form90CRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public Form90CResponse getForm(String email) throws TaxTrackerException {
        Form90CEntity entity = form90CRepository.findTopByEmailOrderByFormIdDesc(email)
                .orElseThrow(() -> new ResourceNotFoundException("app.message.form.not.found"));
        return toResponse(entity);
    }

    private Form90CResponse toResponse(Form90CEntity entity) {
        Form90CResponse response = new Form90CResponse();
        response.setFormId(entity.getFormId());
        response.setEmail(entity.getEmail());
        response.setName(entity.getName());
        response.setMobileNumber(entity.getMobileNumber());
        response.setFinancialYear(entity.getFinancialYear());
        response.setStatus(entity.getStatus());
        response.setDocumentName(entity.getDocumentName());
        List<Form90CTransactionDTO> rows = new ArrayList<>();
        if (entity.getTransactionHistory() != null) {
            rows = entity.getTransactionHistory().stream()
                    .map(r -> modelMapper.map(r, Form90CTransactionDTO.class))
                    .collect(Collectors.toList());
        }
        response.setTransactionHistory(rows);
        return response;
    }
}
