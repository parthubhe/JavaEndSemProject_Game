// src/com/has/mt/GameConfig.java
package com.has.mt;

public class GameConfig {
    // Virtual screen dimensions
    public static final float V_WIDTH = 1920f;
    public static final float V_HEIGHT = 1080f;

    // Physics
    public static final float GRAVITY = -980f; // Pixels per second^2
    public static final float GROUND_Y = 100f; // Base ground level Y-coordinate

    // Player defaults (can be overridden by specific player classes)
    public static final float PLAYER_MOVE_SPEED = 350f;
    public static final float PLAYER_RUN_SPEED = 550f;
    public static final float PLAYER_JUMP_VELOCITY = 600f;
    public static final int PLAYER_START_HEALTH = 100;
    public static final float PLAYER_SCALE = 3.0f; // Visual scale factor

    // Enemy defaults (can be overridden)
    public static final float ENEMY_SCALE = 2.5f; // Visual scale factor for standard enemies
    public static final float ENEMY_DETECT_RANGE = 400f;
    public static final float ENEMY_ATTACK_RANGE = 80f;

    // Projectiles
    public static final float PROJECTILE_SPEED = 600f;
    public static final float PROJECTILE_LIFESPAN = 3.0f; // Seconds before auto-despawn

    // Debugging
    public static final boolean DEBUG_DRAW_BOXES = true; // Draw collision boxes
    public static final boolean DEBUG_DRAW_PATHS = false; // Draw AI paths (if implemented)

    // Level/Background
    public static final float LEVEL_TRANSITION_DISTANCE = 3840f; // Distance before background change/fade
    public static final float FLOOR_PARALLAX_FACTOR = 0.8f; // How fast floor scrolls relative to player
    public static final float FLOOR_TILE_SCALE = 3.0f; // Visual scale of floor tiles

    // UI
    public static final float UI_PADDING = 10f;

    // Add other constants as needed (damage values, cooldowns, etc.)
    public static final int LIGHT_ATTACK_DAMAGE = 15;
    public static final int HEAVY_ATTACK_DAMAGE = 25;
    public static final int PROJECTILE_DAMAGE = 10;
    public static final float ATTACK_COOLDOWN = 0.5f; // Basic cooldown
    public static final float ENEMY_ATTACK_COOLDOWN = 1.5f;
    public static final int ENEMY_BASE_DAMAGE = 10;

    public static final String DEFAULT_PLAYER_NAME = "Player1"; // For Database
}
