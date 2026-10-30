package com.epam.task1.exception;

public class JwtAuthException extends RuntimeException{

    public JwtAuthException(String s) {
        super("JWT auth error occurred " + s);
    }
}
