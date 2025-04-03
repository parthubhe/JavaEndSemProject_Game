// src/com/has/mt/level/SpawnPoint.java
package com.has.mt.level;

// Simple data class for enemy spawn points
public class SpawnPoint {
    public float x, y;
    public String type; // Enemy type string (e.g., "blue_slime")

    public SpawnPoint(float x, float y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
