// src/com/has/mt/managers/EnemyManager.java
package com.has.mt.managers;

import com.badlogic.gdx.Gdx; // Added for logging
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
// import com.badlogic.gdx.math.MathUtils; // Removed if not used
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.DatabaseManager;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.gameobjects.enemies.SlimeEnemy;
// import com.has.mt.gameobjects.enemies.SkeletonEnemy; // Removed if not used yet
import com.has.mt.level.Level;
import com.has.mt.level.LevelData; // Import LevelData
import com.has.mt.level.SpawnPoint;

public class EnemyManager implements Disposable {

    private final AssetLoader assetLoader;
    private final DatabaseManager dbManager;
    private final Array<Enemy> activeEnemies = new Array<>();
    private Character playerTarget;

    public EnemyManager(AssetLoader assetLoader, DatabaseManager dbManager) {
        this.assetLoader = assetLoader;
        this.dbManager = dbManager;
    }

    public void setPlayerTarget(Character player) {
        this.playerTarget = player;
        for(Enemy e : activeEnemies) {
            e.setTarget(player);
        }
    }

    public void spawnEnemiesForLevel(Level level) {
        clearEnemies();
        if (level == null || level.getSpawnPoints() == null) {
            Gdx.app.log("EnemyManager", "Cannot spawn enemies, level or spawn points are null.");
            return;
        }

        // --- FIX: Access level name via LevelData ---
        LevelData data = level.getLevelData();
        String levelName = (data != null && data.levelName != null) ? data.levelName : "Unnamed Level";
        Gdx.app.log("EnemyManager", "Spawning enemies for level: " + levelName);
        // -------------------------------------------

        for (SpawnPoint sp : level.getSpawnPoints()) {
            spawnEnemy(sp.type, sp.x, sp.y);
        }
    }

    public void spawnEnemyAt(String type, float x, float y) {
        spawnEnemy(type, x, y);
    }

    private void spawnEnemy(String type, float x, float y) {
        Enemy enemy = null;
        String lowerCaseType = (type != null) ? type.toLowerCase() : "";

        switch (lowerCaseType) {
            case "blue_slime":
                enemy = new SlimeEnemy(assetLoader, x, y, "Blue");
                break;
            case "green_slime":
                enemy = new SlimeEnemy(assetLoader, x, y, "Green");
                break;
            case "red_slime":
                enemy = new SlimeEnemy(assetLoader, x, y, "Red");
                break;
            // case "skeleton_warrior":
            //     enemy = new SkeletonEnemy(assetLoader, x, y);
            //     break;
            default:
                Gdx.app.error("EnemyManager", "Unknown or null enemy type to spawn: " + type);
                break;
        }

        if (enemy != null) {
            enemy.setTarget(playerTarget);
            activeEnemies.add(enemy);
            Gdx.app.log("EnemyManager", "Spawned " + type + " at (" + x + ", " + y + ")");
        }
    }


    public void update(float delta) {
        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            Enemy e = activeEnemies.get(i);
            e.update(delta);

            if (!e.isAlive() && e.isAnimationFinished(Character.State.DEAD)) { // Use Character.State
                if (!e.isKillProcessed()) {
                    int currentKills = dbManager.getKillCount(GameConfig.DEFAULT_PLAYER_NAME);
                    dbManager.updateKillCount(GameConfig.DEFAULT_PLAYER_NAME, currentKills + 1);
                    Gdx.app.log("EnemyManager", "Enemy kill counted for " + GameConfig.DEFAULT_PLAYER_NAME + ". Total Kills: " + (currentKills + 1));
                    e.markKillProcessed();
                }

                activeEnemies.removeIndex(i);
                e.dispose();
                Gdx.app.log("EnemyManager", "Removed dead enemy.");
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Enemy e : activeEnemies) {
            e.render(batch);
        }
    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        // No changes needed here if GameConfig.DEBUG_DRAW_BOXES handles enabling/disabling
        if (!GameConfig.DEBUG_DRAW_BOXES) return;
        for (Enemy e : activeEnemies) {
            e.drawDebug(shapeRenderer);
        }
    }

    public Array<Enemy> getActiveEnemies() {
        return activeEnemies;
    }

    public void clearEnemies() {
        Gdx.app.log("EnemyManager", "Clearing all enemies.");
        for (Enemy e : activeEnemies) {
            e.dispose();
        }
        activeEnemies.clear();
    }

    @Override
    public void dispose() {
        clearEnemies();
    }
}
