package com.taxtracker.service;

import com.taxtracker.dto.UploadResponse;
import com.taxtracker.exception.TaxTrackerException;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentUploadService {
    UploadResponse upload(String email, MultipartFile file) throws TaxTrackerException;
}
