// src/com/has/mt/level/Level.java
package com.has.mt.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.level.background.ParallaxBackground;
import com.has.mt.level.background.FloorLayer;
// Import ParallaxLayerData if LevelData uses it, even if ParallaxBackground doesn't take it directly
import com.has.mt.level.background.ParallaxLayerData;


public class Level implements Disposable {
    private AssetLoader assetLoader;
    private LevelData levelData;
    private ParallaxBackground background;
    private FloorLayer floor;
    // private TilemapCollision collisionMap;

    public Level(AssetLoader assetLoader, LevelData data) {
        Gdx.app.log("Level", "Creating Level: " + (data != null ? data.levelName : "Unnamed"));
        if (data == null) {
            Gdx.app.error("Level", "LevelData provided was null!");
            this.levelData = new LevelData(); // Use empty data to avoid nulls
        } else {
            this.levelData = data;
        }
        this.assetLoader = assetLoader;

        try {
            // --- FIX: Call ParallaxBackground with ONLY 3 arguments ---
            this.background = new ParallaxBackground(
                assetLoader,
                this.levelData.backgroundTheme,
                this.levelData.backgroundVariant
                // REMOVED: , this.levelData.backgroundLayers
            );
            // ----------------------------------------------------------
            this.floor = new FloorLayer(assetLoader, this.levelData.floorTileIndex);
        } catch (Exception e) {
            Gdx.app.error("Level", "Error creating background/floor", e);
            if (this.background == null) {
                // --- FIX: Call default ParallaxBackground with ONLY 3 arguments ---
                this.background = new ParallaxBackground(assetLoader, null, 0); // Example default
                // REMOVED: , null
                // ----------------------------------------------------------
            }
            if (this.floor == null) this.floor = new FloorLayer(assetLoader, 0);
        }
    }

    public void update(float delta, float cameraX) {
        // Level-specific updates
    }

    public void renderBackground(SpriteBatch batch, float cameraX) {
        if (background != null) {
            background.render(batch, cameraX);
        }
    }

    public void renderFloor(SpriteBatch batch, float cameraX) {
        if (floor != null) {
            floor.render(batch, cameraX);
        }
    }

    public Array<SpawnPoint> getSpawnPoints() {
        return levelData != null ? levelData.spawnPoints : new Array<>();
    }

    public float getLevelWidth() {
        return levelData != null ? levelData.levelWidth : 0;
    }

    public LevelData getLevelData() {
        return this.levelData;
    }

    @Override
    public void dispose() {
        Gdx.app.log("Level", "Disposing Level: " + (levelData != null ? levelData.levelName : "Unnamed"));
        if (background != null) background.dispose();
        if (floor != null) floor.dispose();
        background = null;
        floor = null;
    }
}
