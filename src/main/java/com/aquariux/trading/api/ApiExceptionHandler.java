package com.aquariux.trading.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
class ApiExceptionHandler {

    private static final int MAX_LOG_MESSAGE_LENGTH = 200;

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception, HttpServletRequest request) {
        return badRequest(exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(error -> error.getField()))
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return badRequest(message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleUnreadableJson(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return badRequest("Request JSON is invalid", request);
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String message, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn(
                "Bad request path={} status={} message={}",
                request.getRequestURI(),
                status.value(),
                sanitizeLogMessage(message));
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .build());
    }

    private String sanitizeLogMessage(String message) {
        if (message == null) {
            return "";
        }

        String sanitized = message.replaceAll("\\p{Cntrl}", "?");
        if (sanitized.length() <= MAX_LOG_MESSAGE_LENGTH) {
            return sanitized;
        }
        return sanitized.substring(0, MAX_LOG_MESSAGE_LENGTH) + "...";
    }
}
