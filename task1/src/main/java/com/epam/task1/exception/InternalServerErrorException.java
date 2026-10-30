package com.epam.task1.exception;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String s) {
        super("Internal server error. " + s);
    }
}
