package com.example.petsitter.common.exception;

public class ForbiddenException extends UnauthorizedException {

    public ForbiddenException(String reason, String detail) {
        super(reason, detail);
    }
}
