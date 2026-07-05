package com.taxtracker.exception;

/**
 * Thrown when a transactional email (e.g. the registration OTP) could not be
 * delivered. Unchecked so the EmailService method signatures stay simple; the
 * GlobalExceptionHandler maps it to a clear HTTP response. The message is a
 * property KEY resolved from application.properties.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String messageKey) {
        super(messageKey);
    }
}
