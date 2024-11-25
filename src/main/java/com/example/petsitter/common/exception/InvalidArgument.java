package com.example.petsitter.common.exception;

public record InvalidArgument(String objectName, String fieldName, String detail) {

    public InvalidArgument(String objectName, String detail) {
        this(objectName, null, detail);
    }

    public static final String BLANK_VALUE_MSG = "cannot be blank";

    public static final String NULL_VALUE_MSG = "cannot be null";
}
