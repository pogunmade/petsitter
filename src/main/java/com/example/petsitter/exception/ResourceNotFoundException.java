package com.example.petsitter.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, UUID id) {
        super("Could not find " + resourceName + " " + id);
    }
}
