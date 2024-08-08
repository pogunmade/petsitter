package com.example.petsitter.controller;

import com.example.petsitter.dto.ErrorDTO;
import com.example.petsitter.exception.InvalidRequestException;
import com.example.petsitter.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO resourceNotFoundHandler(ResourceNotFoundException resourceNotFoundException) {

        return new ErrorDTO(resourceNotFoundException.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO invalidRequestHandler(InvalidRequestException invalidRequestException) {

        return new ErrorDTO(invalidRequestException.getMessage());
    }
}
