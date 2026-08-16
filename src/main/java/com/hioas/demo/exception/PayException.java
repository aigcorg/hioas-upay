package com.hioas.demo.exception;

import com.hioas.demo.dto.ApiResponse;
import org.springframework.http.HttpStatus;

public class PayException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public PayException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public PayException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
