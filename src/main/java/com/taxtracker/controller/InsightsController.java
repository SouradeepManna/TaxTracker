package com.taxtracker.controller;

import com.taxtracker.dto.InsightsResponse;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.exception.UnauthorizedException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.InsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    @Autowired
    private InsightsService insightsService;

    @Autowired
    private AuthContext authContext;

    @GetMapping
    public ResponseEntity<InsightsResponse> getInsights(
            @RequestParam(value = "financialYear", required = false) String financialYear)
            throws TaxTrackerException {
        if (!authContext.isAuthenticated()) {
            throw new UnauthorizedException("app.message.unauthorized.access");
        }
        return ResponseEntity.ok(insightsService.getInsights(authContext.getEmail(), financialYear));
    }
}
