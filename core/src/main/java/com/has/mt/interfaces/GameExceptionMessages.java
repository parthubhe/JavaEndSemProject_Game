package com.has.mt.interfaces;

/**
 * Defines constants for common error messages used in custom exceptions.
 */
public interface GameExceptionMessages {
    String ASSET_NOT_FOUND = "Required asset file not found: ";
    String ASSET_LOAD_FAILED = "Failed to load asset: ";
    String SKIN_LOAD_FAILED = "Critical UI Skin failed to load.";
    String DATABASE_CONNECTION_FAILED = "Could not establish database connection.";
    String DATABASE_QUERY_FAILED = "Database query failed: ";
    String DATABASE_UPDATE_FAILED = "Database update failed: ";
    String INVALID_PLAYER_TYPE = "Invalid or unsupported player character type specified: ";
    String INVALID_ENEMY_TYPE = "Invalid or unsupported enemy type specified for spawning: ";
    String NULL_DEPENDENCY = "Critical dependency is null: ";
    String LEVEL_DATA_INVALID = "Level data is missing or invalid.";
    String ANIMATION_SETUP_FAILED = "Failed to setup required animations for character: ";
    String CONFIGURATION_ERROR = "Game configuration error: ";
    String IO_ERROR = "File Input/Output error occurred: "; // Added
    String INITIALIZATION_FAILED = "Initialization failed for component: "; // Added
    String UNEXPECTED_ERROR = "An unexpected error occurred: "; // Added
}
