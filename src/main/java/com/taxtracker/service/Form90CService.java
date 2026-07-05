package com.taxtracker.service;

import com.taxtracker.dto.Form90CRequest;
import com.taxtracker.dto.Form90CResponse;
import com.taxtracker.exception.TaxTrackerException;

public interface Form90CService {
    // status = DRAFT (save for later) or SUBMITTED
    Form90CResponse saveForm(String email, Form90CRequest request, String status) throws TaxTrackerException;

    Form90CResponse getForm(String email) throws TaxTrackerException;
}
