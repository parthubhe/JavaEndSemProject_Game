package com.has.mt;

import com.has.mt.interfaces.GameExceptionMessages; // Correct import path

// Custom exception thrown when a required file is missing
public class CustomFileNotFoundException extends Exception implements GameExceptionMessages { // Implement interface
    public CustomFileNotFoundException(String fileName) {
        // Use message from interface and append filename
        super(GameExceptionMessages.ASSET_NOT_FOUND + fileName);
    }
    public CustomFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
