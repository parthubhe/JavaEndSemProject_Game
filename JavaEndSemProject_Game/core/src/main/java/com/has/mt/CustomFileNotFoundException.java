package com.has.mt;

// NEW: Custom exception thrown when a required file is missing
public class CustomFileNotFoundException extends Exception {
    public CustomFileNotFoundException(String message) {
        super(message);
    }
}
