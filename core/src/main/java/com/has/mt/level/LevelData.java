package com.has.mt.level;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
// --- CHANGE START ---
// ParallaxLayerData is currently unused by ParallaxBackground constructor, but keep class if needed elsewhere
import com.has.mt.level.background.ParallaxLayerData;
// --- CHANGE END ---


public class LevelData {
    public String levelName;
    public String backgroundTheme;
    public int backgroundVariant;
    @Deprecated // Not currently used by ParallaxBackground constructor
    public Array<ParallaxLayerData> backgroundLayers; // Data for parallax (Keep for potential future use)
    public int floorTileIndex;
    public Array<SpawnPoint> spawnPoints;
    public float levelWidth;
    public Vector2 playerStartPos;

    public LevelData() {
        backgroundLayers = new Array<>();
        spawnPoints = new Array<>();
        playerStartPos = new Vector2();
    }
}
