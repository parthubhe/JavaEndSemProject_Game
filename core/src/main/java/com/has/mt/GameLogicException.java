package com.has.mt;

import com.has.mt.interfaces.GameExceptionMessages;

/**
 * A general exception for game logic errors.
 */
public class GameLogicException extends RuntimeException implements GameExceptionMessages {

    public GameLogicException(String message) {
        super(message);
    }

    public GameLogicException(String message, Throwable cause) {
        super(message, cause);
    }

    // Convenience constructor using interface messages
    public GameLogicException(String messageKey, String detail) {
        super(messageKey + detail);
    }
    public GameLogicException(String messageKey, String detail, Throwable cause) {
        super(messageKey + detail, cause);
    }
}
