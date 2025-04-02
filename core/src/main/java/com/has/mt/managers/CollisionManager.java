// src/com/has/mt/managers/CollisionManager.java
package com.has.mt.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
// import com.badlogic.gdx.utils.Array; // Not directly used if iterating via managers
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.gameobjects.Projectile;
import com.has.mt.gameobjects.Character; // For Character states

public class CollisionManager {

    private Player player;
    private EnemyManager enemyManager;
    private ProjectileManager projectileManager;
    // private LevelManager levelManager; // Needed for tilemap collisions

    // Add safety checks in constructor
    public CollisionManager(Player player, EnemyManager enemyManager, ProjectileManager projectileManager /*, LevelManager levelManager*/) {
        if (player == null) {
            Gdx.app.error("CollisionManager", "Cannot initialize with null player!");
            // Optionally throw exception if critical
        }
        if (enemyManager == null) {
            Gdx.app.error("CollisionManager", "Cannot initialize with null enemyManager!");
        }
        if (projectileManager == null) {
            Gdx.app.error("CollisionManager", "Cannot initialize with null projectileManager!");
        }

        this.player = player;
        this.enemyManager = enemyManager;
        this.projectileManager = projectileManager;
        // this.levelManager = levelManager;
    }

    public void checkCollisions() {
        // Check if essential components are null or player is dead
        if (player == null || !player.isAlive() || enemyManager == null || projectileManager == null) {
            return; // Cannot perform checks
        }

        checkPlayerEnemyCollisions();
        checkProjectileCollisions(); // Combined projectile checks
        // checkCharacterWorldCollisions(); // Player and enemy vs level tiles
    }

    private void checkPlayerEnemyCollisions() {
        Rectangle playerBounds = player.bounds;
        if (playerBounds == null) return; // Safety check

        // Iterate through active enemies safely
        if (enemyManager.getActiveEnemies() == null) return;

        for (Enemy enemy : enemyManager.getActiveEnemies()) {
            if (enemy == null || !enemy.isAlive() || enemy.bounds == null || enemy.healthComponent == null) continue; // Safety checks

            Rectangle enemyBounds = enemy.bounds;

            if (Intersector.overlaps(playerBounds, enemyBounds)) {

                // --- Player attacking Enemy ---
                // **** FIX: Use public getter isAttacking() ****
                if (player.isAttacking()) {
                    // ******************************************
                    // Determine damage based on player attack state
                    int damage = 0;
                    Character.State pState = player.getCurrentState();
                    if (pState == Character.State.LIGHT_ATTACK) damage = GameConfig.LIGHT_ATTACK_DAMAGE;
                    else if (pState == Character.State.HEAVY_ATTACK) damage = GameConfig.HEAVY_ATTACK_DAMAGE;
                    else if (pState == Character.State.VADERSTRIKE) damage = GameConfig.HEAVY_ATTACK_DAMAGE * 2; // Example damage values

                    // Check if enemy was recently hit (needs isRecentlyHit() method in HealthComponent or Enemy)
                    // boolean enemyCanBeHit = !enemy.healthComponent.isRecentlyHit(); // Assuming method exists
                    boolean enemyCanBeHit = true; // Placeholder - apply damage regardless for now

                    if (damage > 0 && enemyCanBeHit) {
                        // TODO: Refine with attack hitboxes, timing, and enemy invulnerability
                        enemy.takeDamage(damage);
                        // Gdx.app.log("CollisionManager", "Player ("+ pState +") hit Enemy. Enemy Health: " + enemy.healthComponent.getCurrentHealth());
                    }
                }

                // --- Enemy attacking Player ---
                if (enemy.isAttacking()) {
                    // Player's takeDamage() method handles invulnerability check
                    player.takeDamage(enemy.getAttackDamage());
                    // Log is handled within Player.takeDamage() if implemented there
                    // Gdx.app.log("CollisionManager", "Enemy potentially hit Player.");
                }
            }
        }
    }

    // Combined projectile checks
    private void checkProjectileCollisions() {
        if (projectileManager.getActiveProjectiles() == null) return;

        // Iterate backwards for safe removal if projectiles become inactive during loop
        for (int i = projectileManager.getActiveProjectiles().size - 1; i >= 0; i--) {
            Projectile projectile = projectileManager.getActiveProjectiles().get(i);

            // Initial projectile checks
            if (projectile == null || !projectile.isActive() || projectile.bounds == null) continue;

            Rectangle projectileBounds = projectile.bounds;
            Character owner = projectile.getOwner(); // Get owner once

            // --- Projectile vs Enemy (if shot by player) ---
            if (owner == player) {
                if (enemyManager.getActiveEnemies() == null) continue; // Check enemy list exists

                for (Enemy enemy : enemyManager.getActiveEnemies()) {
                    if (enemy == null || !enemy.isAlive() || enemy.bounds == null || enemy.healthComponent == null) continue;

                    // boolean enemyCanBeHit = !enemy.healthComponent.isRecentlyHit(); // Check enemy invulnerability
                    boolean enemyCanBeHit = true; // Placeholder

                    if (enemyCanBeHit && Intersector.overlaps(projectileBounds, enemy.bounds)) {
                        enemy.takeDamage(projectile.getDamage());
                        projectile.setActive(false); // Deactivate projectile
                        Gdx.app.log("CollisionManager", "Player Projectile hit Enemy. Enemy Health: " + enemy.healthComponent.getCurrentHealth());
                        break; // Projectile hits one enemy and disappears
                    }
                }
            }
            // --- Projectile vs Player (if shot by enemy) ---
            else if (owner instanceof Enemy) { // Check owner type
                if (player.bounds != null && Intersector.overlaps(projectileBounds, player.bounds)) {
                    // Player's takeDamage() handles invulnerability check
                    player.takeDamage(projectile.getDamage());
                    projectile.setActive(false); // Deactivate projectile on hit
                    // Log moved to Player.takeDamage()
                }
            }

            // If projectile is now inactive after checks, no need to check further against others in this frame
            if (!projectile.isActive()) {
                // Projectile might be removed from list by ProjectileManager.update() later,
                // but setting active=false prevents further collisions this frame.
                continue;
            }
        }
    }


    // private void checkCharacterWorldCollisions() {
    //    // Implementation for checking collisions with map tiles
    // }
}
