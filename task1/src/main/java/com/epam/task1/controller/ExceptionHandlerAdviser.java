package com.epam.task1.controller;

import com.epam.task1.exception.InternalServerErrorException;
import com.epam.task1.exception.JwtAuthException;
import com.epam.task1.exception.NoContentException;
import com.epam.task1.exception.NonExistentCustomerException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@ControllerAdvice(basePackages = "com.epam.task1.controller")
public class ExceptionHandlerAdviser {

    @ExceptionHandler(NonExistentCustomerException.class)
    public ResponseEntity<String> handleNonExistentCustomer(
            NonExistentCustomerException exception) {
        log.error("Non existent customer requested.", exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<String> handleInternalServerErrorException(
            InternalServerErrorException exception) {
        log.error("An internal server error occurred. ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<String> handleNoContentException(NoContentException e) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
