package com.taxtracker.controller;

import com.taxtracker.dto.UploadResponse;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.exception.UnauthorizedException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.DocumentUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class DocumentUploadController {

    @Autowired
    private DocumentUploadService documentUploadService;

    @Autowired
    private AuthContext authContext;

    @PostMapping
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file)
            throws TaxTrackerException {
        if (!authContext.isAuthenticated()) {
            throw new UnauthorizedException("app.message.unauthorized.access");
        }
        return ResponseEntity.ok(documentUploadService.upload(authContext.getEmail(), file));
    }
}
