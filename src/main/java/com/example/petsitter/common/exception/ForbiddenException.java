package com.example.petsitter.common.exception;

public class ForbiddenException extends UnauthorizedException {

    public static final String INVALID_VALUE_MSG = "Invalid value";

    public ForbiddenException(String reason, String detail) {
        super(reason, detail);
    }
}
