package com.traazu.auth_service.controllers;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.traazu.auth_service.domain.dtos.ErrorResponse;
import com.traazu.auth_service.services.auth.exceptions.*;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedAccount(AccountLockedException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.LOCKED.value(),
                                            "account locked",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.LOCKED);

    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.UNAUTHORIZED.value(),
                                            "UNAUTHORIZED",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.CONFLICT.value(),
                                            "conflict",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);

    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.BAD_REQUEST.value(),
                                            "bad request",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(InvalidRegistrationTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRegistrationToken(
                InvalidRegistrationTokenException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.BAD_REQUEST.value(),
                                            "bad request",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.BAD_REQUEST.value(),
                                            "bad request",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(
                PasswordMismatchException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.BAD_REQUEST.value(),
                                            "bad request",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.BAD_REQUEST.value(),
                                            "bad request",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(RoleNotSetException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotSet(RoleNotSetException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.BAD_REQUEST.value(),
                                            "bad request",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(TokenMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTokenMismatch(TokenMismatchException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.BAD_REQUEST.value(),
                                            "bad request",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(
                TooManyRequestsException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.TOO_MANY_REQUESTS.value(),
                                            "too many requests",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                                            LocalDateTime.now(),
                                            HttpStatus.NOT_FOUND.value(),
                                            "not found",
                                            ex.getMessage(),
                                            request.getRequestURI()
                                        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Error",
                                message,
                                request.getRequestURI()
                            );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(
            Exception ex, HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}
