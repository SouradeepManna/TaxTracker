package com.taxtracker.service;

import com.taxtracker.dto.InsightsResponse;
import com.taxtracker.exception.TaxTrackerException;

public interface InsightsService {
    InsightsResponse getInsights(String email, String financialYear) throws TaxTrackerException;
}
