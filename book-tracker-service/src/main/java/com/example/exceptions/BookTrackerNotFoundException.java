package com.example.exceptions;

public class BookTrackerNotFoundException extends RuntimeException {
    public BookTrackerNotFoundException(String message) {
        super(message);
    }
}
