package com.epam.task1.exception;

public class NonExistentCustomerException extends RuntimeException{

    public NonExistentCustomerException(Integer id) {
        super("Customer with id " + id + " not found");
    }
}
