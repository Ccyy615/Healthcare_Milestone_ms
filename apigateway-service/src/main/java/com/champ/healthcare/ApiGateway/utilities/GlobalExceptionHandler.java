package com.champ.healthcare.ApiGateway.utilities;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidInput(
            InvalidInputException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleDownstreamService(
            DownstreamServiceException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String requestUri
    ) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(requestUri)
                .details(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
