package com.pix_service.infrastructure.exception;

import org.springframework.http.HttpStatus;

public record Error(String message, HttpStatus httpStatus) {
    public Error(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
