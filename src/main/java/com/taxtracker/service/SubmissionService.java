package com.taxtracker.service;

import com.taxtracker.dto.ApiResponse;
import com.taxtracker.exception.TaxTrackerException;

public interface SubmissionService {
    ApiResponse submit(String email, Long formId) throws TaxTrackerException;
}
