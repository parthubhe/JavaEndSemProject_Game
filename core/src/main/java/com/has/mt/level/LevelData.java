// src/com/has/mt/level/LevelData.java
package com.has.mt.level;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.has.mt.level.background.ParallaxLayerData; // Needs class

// Simple data class to hold level definition
public class LevelData {
    public String levelName;
    public String backgroundTheme; // e.g., "Winter", "Castle"
    public int backgroundVariant;  // e.g., 1 for "winter 1", 2 for "winter 2"
    public Array<ParallaxLayerData> backgroundLayers; // Data for parallax
    public int floorTileIndex;     // Index for the floor texture
    public Array<SpawnPoint> spawnPoints; // Enemy spawn definitions
    public float levelWidth; // Optional: total width of the level area
    public Vector2 playerStartPos; // Where the player starts in this level

    public LevelData() {
        backgroundLayers = new Array<>();
        spawnPoints = new Array<>();
        playerStartPos = new Vector2();
    }
}
