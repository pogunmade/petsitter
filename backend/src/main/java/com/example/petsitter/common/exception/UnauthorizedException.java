package com.example.petsitter.common.exception;

public class UnauthorizedException extends RuntimeException {

    public static final String CREATE_MSG = "Cannot create";
    public static final String DELETE_MSG = "Cannot delete";
    public static final String MODIFY_MSG = "Cannot modify";
    public static final String VIEW_MSG = "Cannot view";

    public UnauthorizedException(String reason, String detail) {
        super(reason + " - " + detail);
    }

    public static String messageValueOf(String reason, String detail) {
        return reason + " - " + detail;
    }
}
