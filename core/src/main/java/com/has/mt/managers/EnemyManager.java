package com.has.mt.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer; // Import Timer
import com.badlogic.gdx.math.MathUtils; // Import MathUtils
import com.badlogic.gdx.math.Vector2; // Not strictly needed here anymore
import java.util.Set; // --- CHANGE START: Import Set ---
import java.util.HashSet; // --- CHANGE START: Import HashSet ---
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.interfaces.GameExceptionMessages;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.gameobjects.enemies.MinotaurEnemy;
import com.has.mt.gameobjects.enemies.SkeletonEnemy;
import com.has.mt.gameobjects.enemies.SlimeEnemy;
import com.has.mt.level.Level;
import com.has.mt.level.LevelData;
import com.has.mt.level.SpawnPoint;


public class EnemyManager implements Disposable {
    private final AssetLoader assetLoader;
    private final Array<Enemy> activeEnemies = new Array<>();
    private Character playerTarget;
    private int enemiesKilledThisSession = 0;

    // Continuous Spawning Logic
    private static final int MAX_ACTIVE_ENEMIES = 8;
    private static final float SPAWN_INTERVAL_MIN = 2.0f;
    private static final float SPAWN_INTERVAL_MAX = 5.0f;
    private static final float SPAWN_DISTANCE_X = GameConfig.V_WIDTH * 0.7f;
    private Timer.Task spawnTask;
    private boolean allowSpawning = false; // Start paused until level loads
    private Array<String> availableEnemyTypes = new Array<>();


    public EnemyManager(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
        // Don't start spawning immediately, wait for level load
    }

    // Methods to control spawning
    public void stopSpawning() {
        allowSpawning = false;
        if (spawnTask != null && spawnTask.isScheduled()) { // Check if scheduled before cancelling
            spawnTask.cancel();
            Gdx.app.log("EnemyManager", "Continuous spawning stopped.");
        }
        spawnTask = null; // Clear task reference
    }

    public void startSpawning() {
        if (allowSpawning) return; // Don't restart if already allowed (prevents duplicate timers)

        allowSpawning = true;
        // Check if task exists and is NOT scheduled before creating a new one
        if (spawnTask == null || !spawnTask.isScheduled()) {
            scheduleNextSpawn();
            Gdx.app.log("EnemyManager", "Continuous spawning started/resumed.");
        }
    }

    private void scheduleNextSpawn() {
        if (!allowSpawning) return; // Double check

        float delay = MathUtils.random(SPAWN_INTERVAL_MIN, SPAWN_INTERVAL_MAX);
        // Ensure previous task is cancelled before scheduling new one
        if (spawnTask != null) {
            spawnTask.cancel();
        }

        spawnTask = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Check conditions inside the task run method
                if (allowSpawning && activeEnemies.size < MAX_ACTIVE_ENEMIES) {
                    spawnRandomEnemyOffScreen();
                }
                // Schedule the *next* spawn only if still allowed
                if (allowSpawning) {
                    scheduleNextSpawn(); // Reschedule from within
                }
            }
        }, delay);
        Gdx.app.debug("EnemyManager", "Next spawn scheduled in " + String.format("%.2f", delay) + "s");
    }

    private void spawnRandomEnemyOffScreen() {
        if (playerTarget == null || playerTarget.position == null || availableEnemyTypes.isEmpty()) {
            Gdx.app.debug("EnemyManager", "Cannot spawn random enemy, player target missing or no available types.");
            return; // Cannot determine spawn location or type
        }

        String typeToSpawn = availableEnemyTypes.random();
        float playerX = playerTarget.position.x;
        float spawnX;
        if (MathUtils.randomBoolean()) { // Spawn right
            spawnX = playerX + SPAWN_DISTANCE_X + MathUtils.random(-150, 150); // Increased variance
        } else { // Spawn left
            spawnX = playerX - SPAWN_DISTANCE_X + MathUtils.random(-150, 150);
            spawnX = Math.max(50, spawnX); // Prevent spawning too far left (adjust as needed)
        }
        float spawnY = GameConfig.GROUND_Y;

        Gdx.app.debug("EnemyManager", "Attempting random spawn: " + typeToSpawn + " at X=" + String.format("%.0f", spawnX));
        spawnEnemy(typeToSpawn, spawnX, spawnY); // Use the existing spawn method
    }


    public void setPlayerTarget(Character player) {
        this.playerTarget = player;
        // Update target for existing enemies if needed (though usually set on spawn)
        if(activeEnemies != null) {
            for (Enemy e : activeEnemies) {
                if (e != null && e.getAI() != null) {
                    e.setTarget(player);
                }
            }
        }
    }

    public void spawnEnemiesForLevel(Level level) {
        clearEnemies(); // Clears active enemies, resets kill count, stops spawning timer
        if (level == null) {
            Gdx.app.error("EnemyManager", GameExceptionMessages.LEVEL_DATA_INVALID + "(Level object is null)");
            return;
        }
        LevelData data = level.getLevelData();
        if (data == null || data.spawnPoints == null) {
            Gdx.app.error("EnemyManager", GameExceptionMessages.LEVEL_DATA_INVALID + "(LevelData or SpawnPoints are null)");
            return;
        }

        String levelName = (data.levelName != null) ? data.levelName : "Unnamed Level";
        Gdx.app.log("EnemyManager", "Spawning initial enemies for level: " + levelName);

        // Determine available types from level data for random spawning
        availableEnemyTypes.clear();
        Set<String> uniqueTypes = new HashSet<>(); // Use imported Set/HashSet
        for (SpawnPoint sp : data.spawnPoints) {
            if (sp != null && sp.type != null && !sp.type.trim().isEmpty()) {
                uniqueTypes.add(sp.type.trim().toLowerCase());
                // Spawn the predefined enemies
                spawnEnemy(sp.type, sp.x, sp.y);
            } else {
                Gdx.app.log("EnemyManager", "Skipping invalid spawn point in level: " + levelName);
            }
        }
        if(!uniqueTypes.isEmpty()){
            availableEnemyTypes.addAll(uniqueTypes.toArray(new String[0]));
        } else {
            Gdx.app.log("EnemyManager", "No enemy types defined in spawn points for level " + levelName + ". Using defaults."); // Changed warn to log
            availableEnemyTypes.addAll("blue_slime", "green_slime", "red_slime", "skeleton_warrior", "minotaur_1"); // Fallback
        }
        Gdx.app.log("EnemyManager", "Available random spawn types for this level: " + availableEnemyTypes);

        Gdx.app.log("EnemyManager", "Finished spawning initial enemies for " + levelName + ". Count: " + activeEnemies.size);
        startSpawning(); // Start continuous spawning for this level
    }

    // public spawnEnemyAt remains the same...
    public void spawnEnemyAt(String type, float x, float y) {
        spawnEnemy(type, x, y);
    }

    private Enemy spawnEnemy(String type, float x, float y) {
        Enemy enemy = null;
        if (type == null || type.trim().isEmpty()) {
            Gdx.app.error("EnemyManager", "Attempted to spawn enemy with null or empty type.");
            return null;
        }
        String lowerCaseType = type.trim().toLowerCase();

        try {
            switch (lowerCaseType) {
                case "blue_slime": enemy = new SlimeEnemy(assetLoader, x, y, "Blue"); break;
                case "green_slime": enemy = new SlimeEnemy(assetLoader, x, y, "Green"); break;
                case "red_slime": enemy = new SlimeEnemy(assetLoader, x, y, "Red"); break;
                case "skeleton_warrior": enemy = new SkeletonEnemy(assetLoader, x, y); break;
                case "minotaur_1": enemy = new MinotaurEnemy(assetLoader, x, y); break;
                default:
                    throw new GameLogicException(GameExceptionMessages.INVALID_ENEMY_TYPE, type);
            }
        } catch (GameLogicException e) {
            Gdx.app.error("EnemyManager", "Failed to create enemy: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Gdx.app.error("EnemyManager", "Unexpected error creating enemy type: " + type, e);
            return null;
        }

        if (enemy != null) {
            if (playerTarget != null) {
                enemy.setTarget(playerTarget);
            } else {
                Gdx.app.log("EnemyManager", "Spawned enemy " + type + " but playerTarget is null.");
            }
            activeEnemies.add(enemy);
            // Gdx.app.debug("EnemyManager", "Spawned " + type + " at (" + x + ", " + y + "). Active: " + activeEnemies.size); // Reduce log spam
        }
        return enemy;
    }


    public void update(float delta) {
        if (activeEnemies == null) return;

        for (int i = activeEnemies.size - 1; i >= 0; i--) {
            Enemy e = activeEnemies.get(i);
            if (e == null) {
                activeEnemies.removeIndex(i);
                continue;
            }
            e.update(delta);

            if (!e.isAlive() && e.isAnimationFinished(Character.State.DEAD)) {
                if (!e.isKillProcessed()) {
                    enemiesKilledThisSession++;
                    e.markKillProcessed();
                }
                activeEnemies.removeIndex(i);
                e.dispose();
            }
        }
        // Spawning is handled by the Timer task started in startSpawning/scheduleNextSpawn
    }

    // Renamed for clarity
    public int getKillCountThisSession() {
        return enemiesKilledThisSession;
    }

    public void resetKillCount() {
        enemiesKilledThisSession = 0;
    }

    public int getActiveEnemyCount() {
        return activeEnemies.size;
    }

    public void render(SpriteBatch batch) {
        if (activeEnemies == null) return;
        for (Enemy e : activeEnemies) {
            if (e != null) e.render(batch);
        }
    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        if (!GameConfig.DEBUG_DRAW_BOXES || activeEnemies == null) return;
        for (Enemy e : activeEnemies) {
            if (e != null) e.drawDebug(shapeRenderer);
        }
    }

    public Array<Enemy> getActiveEnemies() {
        return activeEnemies;
    }

    public void clearEnemies() {
        Gdx.app.log("EnemyManager", "Clearing all enemies (" + activeEnemies.size + ").");
        stopSpawning(); // Stop spawning when clearing
        if (activeEnemies == null) return;
        for (Enemy e : activeEnemies) {
            if (e != null) e.dispose();
        }
        activeEnemies.clear();
        resetKillCount();
    }

    @Override
    public void dispose() {
        Gdx.app.log("EnemyManager", "Disposing Enemy Manager.");
        stopSpawning();
        clearEnemies();
        Timer.instance().clear(); // Clear any remaining tasks globally (use with caution if other timers exist)
        Gdx.app.log("EnemyManager", "Enemy Manager Dispose finished.");
    }
}
