package com.swappy.advice;

import java.util.List;

import org.springframework.http.HttpStatus;

public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> subErrors;

    // No-args constructor
    public ApiError() {
    }

    // All-args constructor
    public ApiError(HttpStatus status, String message, List<String> subErrors) {
        this.status = status;
        this.message = message;
        this.subErrors = subErrors;
    }

    // Getters and Setters
    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getSubErrors() {
        return subErrors;
    }

    public void setSubErrors(List<String> subErrors) {
        this.subErrors = subErrors;
    }
}