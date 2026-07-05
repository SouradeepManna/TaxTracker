package com.taxtracker.exception;

import com.taxtracker.dto.ErrorResponse;
import com.taxtracker.util.MessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.stream.Collectors;

// Centralized exception handling: translates exceptions into HTTP responses
// and resolves message KEYS from application.properties (messages not hardcoded).
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageResolver messageResolver;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        String message = messageResolver.resolve(ex.getMessage());
        return build(HttpStatus.NOT_FOUND, List.of(message));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        String message = messageResolver.resolve(ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, List.of(message));
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException ex) {
        String message = messageResolver.resolve(ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, List.of(message));
    }

    @ExceptionHandler(TaxTrackerException.class)
    public ResponseEntity<ErrorResponse> handleTaxTracker(TaxTrackerException ex) {
        String message = messageResolver.resolve(ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, List.of(message));
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleEmailDelivery(EmailDeliveryException ex) {
        String message = messageResolver.resolve(ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, List.of(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, messages);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSize(MaxUploadSizeExceededException ex) {
        String message = messageResolver.resolve("app.message.file.size.exceeded");
        return build(HttpStatus.BAD_REQUEST, List.of(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        String message = messageResolver.resolve("app.message.generic.error");
        return build(HttpStatus.INTERNAL_SERVER_ERROR, List.of(message));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, List<String> messages) {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), messages);
        return ResponseEntity.status(status).body(body);
    }
}
