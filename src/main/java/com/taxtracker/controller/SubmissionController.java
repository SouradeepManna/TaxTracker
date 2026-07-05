package com.taxtracker.controller;

import com.taxtracker.dto.ApiResponse;
import com.taxtracker.dto.SubmissionRequest;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.exception.UnauthorizedException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private AuthContext authContext;

    @PostMapping
    public ResponseEntity<ApiResponse> submit(@Valid @RequestBody SubmissionRequest request)
            throws TaxTrackerException {
        if (!authContext.isAuthenticated()) {
            throw new UnauthorizedException("app.message.unauthorized.access");
        }
        return ResponseEntity.ok(submissionService.submit(authContext.getEmail(), request.getFormId()));
    }
}
