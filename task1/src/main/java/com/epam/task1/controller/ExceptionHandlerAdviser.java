package com.epam.task1.controller;

import com.epam.task1.exception.InternalServerErrorException;
import com.epam.task1.exception.JwtAuthException;
import com.epam.task1.exception.NonExistentCustomerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice(basePackages = "com.epam.task1.controller")
public class ExceptionHandlerAdviser {

    @ExceptionHandler(NonExistentCustomerException.class)
    public ResponseEntity<String> handleNonExistentCustomer(
            NonExistentCustomerException exception) {
        log.error("Non existent customer requested.", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<String> handleInternalServerErrorException(
            InternalServerErrorException exception) {
        log.error("An internal server error occurred. ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler(JwtAuthException.class)
    public ResponseEntity<String> handleJwtAuthException(JwtAuthException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
}
