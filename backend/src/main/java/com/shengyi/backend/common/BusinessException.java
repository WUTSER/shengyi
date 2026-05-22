package com.shengyi.backend.common;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException("400", message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException("404", message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException("409", message, HttpStatus.CONFLICT);
    }

    public static BusinessException validation(String message) {
        return new BusinessException("422", message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
