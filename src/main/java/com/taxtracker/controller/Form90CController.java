package com.taxtracker.controller;

import com.taxtracker.dto.Form90CRequest;
import com.taxtracker.dto.Form90CResponse;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.exception.UnauthorizedException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.Form90CService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forms/90c")
public class Form90CController {

    @Autowired
    private Form90CService form90CService;

    @Autowired
    private AuthContext authContext;

    // US06: fill + save (DRAFT) OR submit details. status param: DRAFT | SUBMITTED
    @PostMapping
    public ResponseEntity<Form90CResponse> saveForm(
            @Valid @RequestBody Form90CRequest request,
            @RequestParam(defaultValue = "DRAFT") String status) throws TaxTrackerException {
        requireAuth();
        return ResponseEntity.ok(form90CService.saveForm(authContext.getEmail(), request, status));
    }

    @GetMapping("/{emailId}")
    public ResponseEntity<Form90CResponse> getForm(@PathVariable String emailId) throws TaxTrackerException {
        requireAuth();
        return ResponseEntity.ok(form90CService.getForm(emailId));
    }

    // convenience: fetch current user's form
    @GetMapping("/me")
    public ResponseEntity<Form90CResponse> getMyForm() throws TaxTrackerException {
        requireAuth();
        return ResponseEntity.ok(form90CService.getForm(authContext.getEmail()));
    }

    private void requireAuth() throws TaxTrackerException {
        if (!authContext.isAuthenticated()) {
            throw new UnauthorizedException("app.message.unauthorized.access");
        }
    }
}
