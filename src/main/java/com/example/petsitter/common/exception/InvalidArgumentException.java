package com.example.petsitter.common.exception;

import lombok.Getter;

import java.util.List;

public class InvalidArgumentException extends RuntimeException {

    @Getter
    private final List<InvalidArgument> invalidArguments;

    public InvalidArgumentException(String objectName, String detail) {
        this(objectName, null, detail);
    }

    public InvalidArgumentException(String objectName, String fieldName, String detail) {
        super("Invalid argument(s)");

        invalidArguments = List.of(new InvalidArgument(objectName, fieldName, detail));
    }

    public InvalidArgumentException(List<InvalidArgument> invalidArguments) {
        super("Invalid argument(s)");

        this.invalidArguments = invalidArguments;
    }

    public boolean contains(String objectName, String detail) {
        return invalidArguments.contains(new InvalidArgument(objectName, detail));
    }

    public boolean contains(String objectName, String fieldName, String detail) {
        return invalidArguments.contains(new InvalidArgument(objectName, fieldName, detail));
    }
}
