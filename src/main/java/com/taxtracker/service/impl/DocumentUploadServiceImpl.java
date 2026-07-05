package com.taxtracker.service.impl;

import com.taxtracker.dto.UploadResponse;
import com.taxtracker.entity.Form90CEntity;
import com.taxtracker.entity.UploadedDocumentEntity;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.repository.Form90CRepository;
import com.taxtracker.repository.UploadedDocumentRepository;
import com.taxtracker.service.DocumentUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class DocumentUploadServiceImpl implements DocumentUploadService {

    private static final long MAX_SIZE = 2L * 1024 * 1024; // 2MB
    private static final List<String> ALLOWED = Arrays.asList(
            "application/pdf", "image/jpeg", "image/jpg");

    @Autowired
    private UploadedDocumentRepository uploadedDocumentRepository;

    @Autowired
    private Form90CRepository form90CRepository;

    @Override
    public UploadResponse upload(String email, MultipartFile file) throws TaxTrackerException {
        if (file == null || file.isEmpty()) {
            throw new InvalidInputException("app.message.file.not.uploaded");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new InvalidInputException("app.message.file.size.exceeded");
        }
        String contentType = file.getContentType();
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean validType = contentType != null && ALLOWED.contains(contentType.toLowerCase());
        boolean validExt = name.endsWith(".pdf") || name.endsWith(".jpg") || name.endsWith(".jpeg");
        if (!validType || !validExt) {
            throw new InvalidInputException("app.message.file.invalid.format");
        }

        UploadedDocumentEntity doc = new UploadedDocumentEntity();
        doc.setEmail(email);
        doc.setFileName(file.getOriginalFilename());
        doc.setContentType(contentType);
        doc.setFileSize(file.getSize());
        try {
            doc.setData(file.getBytes());
        } catch (Exception e) {
            throw new InvalidInputException("app.message.file.invalid.format");
        }
        doc.setUploadedAt(LocalDateTime.now());
        UploadedDocumentEntity saved = uploadedDocumentRepository.save(doc);

        // attach document name to the user's latest form (if any)
        form90CRepository.findTopByEmailOrderByFormIdDesc(email).ifPresent(form -> {
            form.setDocumentName(file.getOriginalFilename());
            form90CRepository.save(form);
        });

        return new UploadResponse(saved.getDocumentId(), saved.getFileName(), "File uploaded successfully.");
    }
}
