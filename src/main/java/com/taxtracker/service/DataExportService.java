package com.taxtracker.service;

import com.taxtracker.exception.TaxTrackerException;

public interface DataExportService {
    // returns the file bytes for the given format (json, pdf, excel)
    byte[] export(String email, String format, String financialYear) throws TaxTrackerException;

    String contentType(String format);

    String fileName(String format);
}
