package com.taxtracker.controller;

import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.exception.UnauthorizedException;
import com.taxtracker.security.AuthContext;
import com.taxtracker.service.DataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class DataExportController {

    @Autowired
    private DataExportService dataExportService;

    @Autowired
    private AuthContext authContext;

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(required = false) String financialYear) throws TaxTrackerException {

        if (!authContext.isAuthenticated()) {
            throw new UnauthorizedException("app.message.unauthorized.access");
        }
        byte[] content = dataExportService.export(authContext.getEmail(), format, financialYear);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + dataExportService.fileName(format) + "\"")
                .contentType(MediaType.parseMediaType(dataExportService.contentType(format)))
                .body(content);
    }
}
