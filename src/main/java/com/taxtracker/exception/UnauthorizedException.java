package com.taxtracker.exception;

public class UnauthorizedException extends TaxTrackerException {
    public UnauthorizedException(String messageKey) {
        super(messageKey);
    }
}
