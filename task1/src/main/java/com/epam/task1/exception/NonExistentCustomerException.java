package com.epam.task1.exception;

public class NonExistentCustomerException extends RuntimeException{

    public NonExistentCustomerException(Long id) {
        super("Customer with id " + id + " not found");
    }
}
