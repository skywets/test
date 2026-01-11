package com.example.test.exceptions;

public class ApplicationAlreadyExistsException extends RuntimeException {
    public ApplicationAlreadyExistsException(String message) {
        super(message);
    }
}
