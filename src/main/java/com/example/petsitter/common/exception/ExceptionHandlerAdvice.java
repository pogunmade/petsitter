package com.example.petsitter.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(1)
class ExceptionHandlerAdvice {

    @ExceptionHandler(value = AuthenticationException.class)
    ProblemDetail authenticationExceptionHandler(AuthenticationException authenticationException) {

        return problemDetail(HttpStatus.UNAUTHORIZED, authenticationException.getMessage(),
            "/errors/unauthorized", null);
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    ProblemDetail unauthorizedExceptionHandler(UnauthorizedException unauthorizedException) {

        return problemDetail(HttpStatus.UNAUTHORIZED, unauthorizedException.getMessage(),
            "/errors/unauthorized", null);
    }

    @ExceptionHandler(value = ForbiddenException.class)
    ProblemDetail forbiddenExceptionHandler(ForbiddenException forbiddenException) {

        return problemDetail(HttpStatus.FORBIDDEN, forbiddenException.getMessage(),
            "/errors/forbidden", null);
    }

    @ExceptionHandler(value = InvalidArgumentException.class)
    ProblemDetail invalidArgumentExceptionHandler(InvalidArgumentException invalidArgumentException) {

        Map<String, Object> invalidArguments = invalidArgumentException.getInvalidArguments().stream()
            .collect(Collectors.toMap(

                invalidArgument -> invalidArgument.objectName() +
                    (invalidArgument.fieldName() != null ? "." + invalidArgument.fieldName() : ""),

                InvalidArgument::detail)
            );

        return problemDetail(HttpStatus.BAD_REQUEST, invalidArgumentException.getMessage(),
            "/errors/bad-request", invalidArguments);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ProblemDetail methodArgumentNotValidExceptionHandler(
        MethodArgumentNotValidException methodArgumentNotValidException) {

        Map<String, Object> validationErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors()
            .stream()
            .collect(Collectors.toMap(

                fieldError -> fieldError.getObjectName().replaceFirst("Dto", "") +
                    "." + fieldError.getField(),

                fieldError -> String.valueOf(fieldError.getDefaultMessage()))
            );

        return problemDetail(HttpStatus.BAD_REQUEST, "Invalid argument(s)",
            "/errors/bad-request", validationErrors);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    ProblemDetail constraintViolationExceptionHandler(ConstraintViolationException constraintViolationException) {

        Map<String, Object> validationErrors = constraintViolationException.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                constraintViolation -> String.valueOf(constraintViolation.getPropertyPath()),
                ConstraintViolation::getMessage)
            );

        return problemDetail(HttpStatus.BAD_REQUEST, "Constraint violation(s)",
            "/errors/bad-request", validationErrors);
    }

    @ExceptionHandler(value = NotFoundException.class)
    ProblemDetail notFoundExceptionHandler(NotFoundException notFoundException) {

        return problemDetail(HttpStatus.NOT_FOUND, "Cannot find resource - " + notFoundException.getMessage(),
            "/errors/not-found", null);
    }

    static ProblemDetail problemDetail(HttpStatus status, String message, String resolutionPath,
                                       Map<String, Object> properties) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);

        problemDetail.setType(ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(resolutionPath).build().toUri());

        problemDetail.setProperty("timestamp", LocalDateTime.now());

        if (properties != null) {
            problemDetail.setProperties(properties);
        }

        return problemDetail;
    }
}

@RestControllerAdvice
@Order(2)
class CatchAllExceptionHandlerAdvice {

    @ExceptionHandler
    ProblemDetail exceptionHandler(RuntimeException exception) {

        return ExceptionHandlerAdvice.problemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getMessage(), "/errors/server-error", null);
    }
}
