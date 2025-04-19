// src/com/has/mt/level/LevelManager.java
package com.has.mt.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.level.background.ParallaxBackground; // Needs class
import com.has.mt.level.background.FloorLayer; // Needs class
import com.has.mt.managers.EnemyManager; // To trigger spawning
import com.has.mt.gameobjects.Player; // To set start position
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.has.mt.GameConfig; // Also needed for GameConfig constants
// Manages loading and transitioning between levels
public class LevelManager implements Disposable {

    private AssetLoader assetLoader;
    private Array<LevelData> availableLevels;
    private Level currentLevel;
    private int currentLevelIndex = -1;

    public LevelManager(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
        this.availableLevels = new Array<>();
        loadLevelDefinitions(); // Load info about all levels
    }

    private void loadLevelDefinitions() {
        // TODO: Load level data from files (JSON, XML, etc.) or define programmatically
        // Example: Define Level 1 (Winter 1)
        LevelData level1 = new LevelData();
        level1.levelName = "Frozen Path";
        level1.backgroundTheme = "Winter";
        level1.backgroundVariant = 1;
        // Define layers for winter 1 background... (path, factor)
        // level1.backgroundLayers.add(new ParallaxLayerData("Backgrounds/Winter/Layered/winter 1/1.png", 0.1f)); ...add all 7
        level1.floorTileIndex = 0; // First floor tile
        level1.playerStartPos.set(200, GameConfig.GROUND_Y);
        level1.spawnPoints.add(new SpawnPoint(800, GameConfig.GROUND_Y, "blue_slime"));
        level1.spawnPoints.add(new SpawnPoint(1200, GameConfig.GROUND_Y, "green_slime"));
        level1.spawnPoints.add(new SpawnPoint(2000, GameConfig.GROUND_Y, "blue_slime"));
        level1.levelWidth = 4000; // Example width
        availableLevels.add(level1);

        // Example: Define Level 2 (Winter 2)
        LevelData level2 = new LevelData();
        level2.levelName = "Icy Ridge";
        level2.backgroundTheme = "Winter";
        level2.backgroundVariant = 2;
        // Define layers for winter 2 background...
        level2.floorTileIndex = 1;
        level2.playerStartPos.set(200, GameConfig.GROUND_Y); // Or connect from previous level end?
        level2.spawnPoints.add(new SpawnPoint(700, GameConfig.GROUND_Y, "green_slime"));
        level2.spawnPoints.add(new SpawnPoint(1500, GameConfig.GROUND_Y, "red_slime"));
        level2.spawnPoints.add(new SpawnPoint(2500, GameConfig.GROUND_Y, "skeleton_warrior")); // Example
        level2.levelWidth = 4000;
        availableLevels.add(level2);

        // Add more levels...
        Gdx.app.log("LevelManager", "Loaded " + availableLevels.size + " level definitions.");
    }

    public boolean loadLevel(int index, Player player, EnemyManager enemyManager) {
        if (index < 0 || index >= availableLevels.size) {
            Gdx.app.error("LevelManager", "Invalid level index requested: " + index);
            return false;
        }
        Gdx.app.log("LevelManager", "Loading level " + index);

        if (currentLevel != null) {
            currentLevel.dispose(); // Dispose previous level resources
        }

        LevelData data = availableLevels.get(index);
        currentLevel = new Level(assetLoader, data); // Level constructor loads its assets
        currentLevelIndex = index;

        // Position player at start
        if (player != null) {
            player.reset(data.playerStartPos.x, data.playerStartPos.y);
        }
        // Spawn enemies for the new level
        if (enemyManager != null) {
            enemyManager.spawnEnemiesForLevel(currentLevel);
        }

        Gdx.app.log("LevelManager", "Level " + index + " loaded successfully.");
        return true;
    }

    public void update(float delta, float cameraX) {
        if (currentLevel != null) {
            currentLevel.update(delta, cameraX);
            // TODO: Check for level transition triggers (e.g., player reaches end of levelWidth)
            // if (cameraX > currentLevel.getLevelWidth() - GameConfig.V_WIDTH / 2) {
            //    loadNextLevel(player, enemyManager);
            // }
        }
    }

    public void renderBackground(SpriteBatch batch, float cameraX) {
        if (currentLevel != null) {
            currentLevel.renderBackground(batch, cameraX);
        }
    }

    public void renderFloor(SpriteBatch batch, float cameraX) {
        if (currentLevel != null) {
            currentLevel.renderFloor(batch, cameraX);
        }
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    @Override
    public void dispose() {
        Gdx.app.log("LevelManager", "Disposing current level.");
        if (currentLevel != null) {
            currentLevel.dispose();
        }
        availableLevels.clear(); // Clear definitions
    }
}
