package com.epam.task3.sport;

public class SportAlreadyExistsException extends RuntimeException {

    private final String sportName;

    public SportAlreadyExistsException(String sportName) {
        super("Sport with name '" + sportName + "' already exists");
        this.sportName = sportName;
    }

    public String getSportName() {
        return sportName;
    }
}
