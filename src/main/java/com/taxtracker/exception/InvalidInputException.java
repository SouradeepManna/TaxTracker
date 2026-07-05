package com.taxtracker.exception;

public class InvalidInputException extends TaxTrackerException {
    public InvalidInputException(String messageKey) {
        super(messageKey);
    }
}
