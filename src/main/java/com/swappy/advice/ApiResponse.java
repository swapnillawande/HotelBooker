package com.swappy.advice;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private LocalDateTime timeStamp;
    private T data;
    private ApiError error;

    // No-args constructor
    public ApiResponse() {
        this.timeStamp = LocalDateTime.now();
    }

    // All-args constructor
    public ApiResponse(LocalDateTime timeStamp, T data, ApiError error) {
        this.timeStamp = timeStamp;
        this.data = data;
        this.error = error;
    }

    // Constructor for success
    public ApiResponse(T data) {
        this.timeStamp = LocalDateTime.now();
        this.data = data;
    }

    // Constructor for error
    public ApiResponse(ApiError error) {
        this.timeStamp = LocalDateTime.now();
        this.error = error;
    }

    // Getters and Setters
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }
}