package com.swappy.advice;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.swappy.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException exception) {

        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                null
        );

        ApiResponse<?> response = new ApiResponse<>(
                LocalDateTime.now(),
                null,
                apiError
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternalServerError(Exception exception) {

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                null
        );

        ApiResponse<?> response = new ApiResponse<>(
                LocalDateTime.now(),
                null,
                apiError
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}