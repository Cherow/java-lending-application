package com.example.main.common;

import com.example.main.common.model.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Resource not found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    // 2. Bad request (your case 🔥)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    // 3. Invalid state (e.g. disburse wrong status)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    // 4. Catch-all (real server errors only)
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiErrorResponse> handleGeneral(
//            Exception ex,
//            HttpServletRequest request
//    ) {
//        return buildResponse(
//                "Something went wrong. Please try again",
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                request
//        );
//    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, status);
    }


}