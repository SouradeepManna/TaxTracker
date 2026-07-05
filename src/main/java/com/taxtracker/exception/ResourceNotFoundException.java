package com.taxtracker.exception;

public class ResourceNotFoundException extends TaxTrackerException {
    public ResourceNotFoundException(String messageKey) {
        super(messageKey);
    }
}
