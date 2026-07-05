package com.taxtracker.exception;

// Custom user-defined exception. The message passed in is a PROPERTY KEY
// (e.g. "app.message.user.already.exists") which is resolved from
// application.properties by the global exception handler. This keeps
// exception messages out of the code (not hardcoded).
public class TaxTrackerException extends Exception {

    public TaxTrackerException(String messageKey) {
        super(messageKey);
    }
}
