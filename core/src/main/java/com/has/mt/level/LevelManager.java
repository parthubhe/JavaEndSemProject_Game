package com.has.mt.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.managers.EnemyManager;
import com.has.mt.gameobjects.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.has.mt.GameConfig;
import com.badlogic.gdx.math.MathUtils; // For randomizing spawns later if needed
import com.has.mt.GameLogicException; // Import exceptions
import com.has.mt.interfaces.GameExceptionMessages;


public class LevelManager implements Disposable {

    private AssetLoader assetLoader;
    private Array<LevelData> availableLevels;
    private Level currentLevel;
    private int currentLevelIndex = -1;
    private Player playerRef;
    private EnemyManager enemyManagerRef;


    public LevelManager(AssetLoader assetLoader) {
        if (assetLoader == null) throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AssetLoader in LevelManager");
        this.assetLoader = assetLoader;
        this.availableLevels = new Array<>();
        loadLevelDefinitions();
    }

    private void loadLevelDefinitions() {
        Gdx.app.log("LevelManager", "Loading hardcoded level definitions...");

        // Level 1 (Winter 1)
        LevelData level1 = new LevelData();
        level1.levelName = "Frozen Path";
        level1.backgroundTheme = "Winter";
        level1.backgroundVariant = 1;
        level1.floorTileIndex = 0;
        level1.playerStartPos.set(200, GameConfig.GROUND_Y);
        level1.levelWidth = 6000;

        level1.spawnPoints.add(new SpawnPoint(800, GameConfig.GROUND_Y, "blue_slime"));
        level1.spawnPoints.add(new SpawnPoint(1200, GameConfig.GROUND_Y, "green_slime"));
        level1.spawnPoints.add(new SpawnPoint(1600, GameConfig.GROUND_Y, "blue_slime"));
        level1.spawnPoints.add(new SpawnPoint(2200, GameConfig.GROUND_Y, "skeleton_warrior"));
        level1.spawnPoints.add(new SpawnPoint(2800, GameConfig.GROUND_Y, "green_slime"));
        level1.spawnPoints.add(new SpawnPoint(3400, GameConfig.GROUND_Y, "red_slime"));
        level1.spawnPoints.add(new SpawnPoint(4000, GameConfig.GROUND_Y, "skeleton_warrior"));
        level1.spawnPoints.add(new SpawnPoint(4600, GameConfig.GROUND_Y, "blue_slime"));
        level1.spawnPoints.add(new SpawnPoint(5200, GameConfig.GROUND_Y, "minotaur_1"));
        availableLevels.add(level1);

        // Level 2 (Winter 2) - Example
        LevelData level2 = new LevelData();
        level2.levelName = "Icy Ridge";
        level2.backgroundTheme = "Winter";
        level2.backgroundVariant = 2;
        level2.floorTileIndex = 1;
        level2.playerStartPos.set(200, GameConfig.GROUND_Y);
        level2.levelWidth = 7000;

        level2.spawnPoints.add(new SpawnPoint(700, GameConfig.GROUND_Y, "green_slime"));
        level2.spawnPoints.add(new SpawnPoint(1100, GameConfig.GROUND_Y, "red_slime"));
        level2.spawnPoints.add(new SpawnPoint(1500, GameConfig.GROUND_Y, "skeleton_warrior"));
        level2.spawnPoints.add(new SpawnPoint(2100, GameConfig.GROUND_Y, "blue_slime"));
        level2.spawnPoints.add(new SpawnPoint(2700, GameConfig.GROUND_Y, "minotaur_1"));
        level2.spawnPoints.add(new SpawnPoint(3300, GameConfig.GROUND_Y, "skeleton_warrior"));
        level2.spawnPoints.add(new SpawnPoint(3900, GameConfig.GROUND_Y, "green_slime"));
        level2.spawnPoints.add(new SpawnPoint(4500, GameConfig.GROUND_Y, "red_slime"));
        level2.spawnPoints.add(new SpawnPoint(5200, GameConfig.GROUND_Y, "skeleton_warrior"));
        level2.spawnPoints.add(new SpawnPoint(5900, GameConfig.GROUND_Y, "blue_slime"));
        level2.spawnPoints.add(new SpawnPoint(6600, GameConfig.GROUND_Y, "minotaur_1"));
        availableLevels.add(level2);

        Gdx.app.log("LevelManager", "Loaded " + availableLevels.size + " level definitions.");
    }

    public boolean loadLevel(int index, Player player, EnemyManager enemyManager) {
        this.playerRef = player;
        this.enemyManagerRef = enemyManager;

        if (index < 0 || index >= availableLevels.size) {
            Gdx.app.error("LevelManager", "Invalid level index requested: " + index);
            return false;
        }
        if (player == null || enemyManager == null) {
            Gdx.app.error("LevelManager", "Cannot load level, Player or EnemyManager is null.");
            return false;
        }

        Gdx.app.log("LevelManager", "Loading level " + index + "...");

        if (currentLevel != null) {
            currentLevel.dispose();
            currentLevel = null;
        }

        LevelData data = availableLevels.get(index);
        if (data == null) {
            Gdx.app.error("LevelManager", "LevelData is null for index: " + index);
            return false;
        }

        try {
            currentLevel = new Level(assetLoader, data);
        } catch (Exception e) {
            Gdx.app.error("LevelManager", "Failed to create Level object for index " + index, e);
            currentLevel = null;
            return false;
        }

        currentLevelIndex = index;

        if (data.playerStartPos != null) {
            player.reset(data.playerStartPos.x, data.playerStartPos.y);
        } else {
            // --- CHANGE: Replace warn with log ---
            Gdx.app.log("LevelManager", "No playerStartPos defined for level " + index + ". Player not repositioned.");
            // --- END CHANGE ---
        }

        enemyManager.spawnEnemiesForLevel(currentLevel);

        Gdx.app.log("LevelManager", "Level '" + data.levelName + "' (Index: " + index + ") loaded successfully.");
        return true;
    }

    public boolean loadNextLevel() {
        if (currentLevelIndex + 1 < availableLevels.size) {
            Gdx.app.log("LevelManager", "Loading next level...");
            return loadLevel(currentLevelIndex + 1, playerRef, enemyManagerRef);
        } else {
            Gdx.app.log("LevelManager", "No more levels available.");
            return false;
        }
    }

    public void update(float delta, float cameraX) {
        if (currentLevel != null) {
            currentLevel.update(delta, cameraX);
            LevelData data = currentLevel.getLevelData();
            if (playerRef != null && data != null && data.levelWidth > 0) {
                float transitionPoint = data.levelWidth - GameConfig.V_WIDTH * 0.2f;
                if (playerRef.position.x > transitionPoint) {
                    if (loadNextLevel()) {
                        Gdx.app.log("LevelManager", "Transitioned to next level.");
                    } else {
                        Gdx.app.log("LevelManager", "Reached end of final level.");
                    }
                }
            }
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
        Gdx.app.log("LevelManager", "Disposing LevelManager...");
        if (currentLevel != null) {
            currentLevel.dispose();
            currentLevel = null;
        }
        if (availableLevels != null) {
            availableLevels.clear();
        }
        playerRef = null;
        enemyManagerRef = null;
        Gdx.app.log("LevelManager", "Dispose finished.");
    }
}
