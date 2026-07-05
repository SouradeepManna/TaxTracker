package com.taxtracker.service.impl;

import com.taxtracker.dto.ApiResponse;
import com.taxtracker.entity.Form90CEntity;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.ResourceNotFoundException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.Form90CRepository;
import com.taxtracker.service.EmailService;
import com.taxtracker.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    private Form90CRepository form90CRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public ApiResponse submit(String email, Long formId) throws TaxTrackerException {
        Form90CEntity form = form90CRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("app.message.form.not.found"));

        if (form.getTransactionHistory() == null || form.getTransactionHistory().isEmpty()) {
            throw new InvalidInputException("app.message.no.transactions.added");
        }
        if (form.getDocumentName() == null || form.getDocumentName().isBlank()) {
            throw new InvalidInputException("app.message.file.not.uploaded");
        }

        form.setStatus("SUBMITTED");
        form90CRepository.save(form);

        // Send a confirmation email after a successful submission.
        emailService.sendForm90CConfirmation(form.getEmail(), form.getName(), form.getFinancialYear());

        return new ApiResponse("Form 90C submitted successfully", true);
    }
}
