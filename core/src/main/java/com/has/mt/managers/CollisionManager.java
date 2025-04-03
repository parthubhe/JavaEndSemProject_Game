package com.has.mt.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.gameobjects.Projectile;
import com.has.mt.gameobjects.Character;

public class CollisionManager {

    private Player player;
    private EnemyManager enemyManager;
    private ProjectileManager projectileManager;

    public CollisionManager(Player player, EnemyManager enemyManager, ProjectileManager projectileManager) {
        if (player == null) { throw new IllegalArgumentException("Player cannot be null for CollisionManager"); }
        if (enemyManager == null) { throw new IllegalArgumentException("EnemyManager cannot be null for CollisionManager"); }
        if (projectileManager == null) { throw new IllegalArgumentException("ProjectileManager cannot be null for CollisionManager"); }

        this.player = player;
        this.enemyManager = enemyManager;
        this.projectileManager = projectileManager;
    }

    public void checkCollisions() {
        if (player == null || enemyManager == null || projectileManager == null) return; // Check managers

        // Check projectile collisions regardless of player state
        checkProjectileCollisions();

        // Only check player-involved collisions if player is valid and alive
        if (!player.isAlive() || player.bounds == null) {
            return;
        }
        checkPlayerEnemyCollisions();

    }

    private void checkPlayerEnemyCollisions() {
        Rectangle playerBounds = player.bounds; // Player validity checked in checkCollisions

        if (enemyManager.getActiveEnemies() == null) return;

        for (Enemy enemy : enemyManager.getActiveEnemies()) {
            if (enemy == null || !enemy.isAlive() || enemy.bounds == null || enemy.healthComponent == null) continue;

            Rectangle enemyBounds = enemy.bounds;

            if (Intersector.overlaps(playerBounds, enemyBounds)) {

                // --- Player attacking Enemy ---
                if (player.isAttacking()) {
                    Character.State pState = player.getCurrentState();
                    if (isMeleeAttackState(pState)) {
                        int damage = getDamageForPlayerState(pState);
                        if (damage > 0) {
                            // TODO: Add player attack hitbox check here instead of just bounds overlap
                            // --- CHANGE START: Check if enemy already hit this attack ---
                            if (!player.hasHitEnemyThisAttack(enemy)) {
                                enemy.takeDamage(damage);
                                player.markEnemyHitThisAttack(enemy); // Mark as hit for this attack sequence
                                Gdx.app.debug("CollisionManager", "Player (" + pState + ") hit Enemy (" + enemy.getClass().getSimpleName() + ") ONCE. Enemy Health: " + enemy.healthComponent.getCurrentHealth());
                            }
                            // --- CHANGE END ---
                        }
                    }
                }

                // --- Enemy attacking Player ---
                Character.State eState = enemy.getCurrentState();
                // Check if enemy is in attack state AND hasn't dealt damage in this specific attack instance yet
                if (enemy.isAttacking() && isMeleeAttackState(eState) && !enemy.hasDealtDamageThisAttack()) {
                    // Player invulnerability is handled within player.takeDamage
                    player.takeDamage(enemy.getAttackDamage());
                    // Mark that this enemy attack instance has now dealt its damage
                    enemy.markDamageDealtThisAttack();
                    Gdx.app.debug("CollisionManager", "Enemy (" + enemy.getClass().getSimpleName() + "/" + eState + ") dealt damage to Player. Player Health: " + player.healthComponent.getCurrentHealth());
                }
            }
        }
    }

    // Combined projectile checks
    private void checkProjectileCollisions() {
        checkProjectileEnemyCollisions();
        // Only check vs player if player is alive
        if (player != null && player.isAlive() && player.bounds != null) {
            checkProjectilePlayerCollisions();
        }
    }

    private void checkProjectileEnemyCollisions() {
        if (projectileManager.getActiveProjectiles() == null || enemyManager.getActiveEnemies() == null) return;

        for (int i = projectileManager.getActiveProjectiles().size - 1; i >= 0; i--) {
            Projectile projectile = projectileManager.getActiveProjectiles().get(i);
            // Check owner is Player
            if (projectile == null || !projectile.isActive() || projectile.bounds == null || !(projectile.getOwner() instanceof Player)) {
                continue;
            }

            Rectangle projectileBounds = projectile.bounds;

            for (Enemy enemy : enemyManager.getActiveEnemies()) {
                if (enemy == null || !enemy.isAlive() || enemy.bounds == null || enemy.healthComponent == null) continue;

                if (Intersector.overlaps(projectileBounds, enemy.bounds)) {
                    enemy.takeDamage(projectile.getDamage());
                    projectile.setActive(false); // Deactivate projectile on hit
                    Gdx.app.debug("CollisionManager", "Player Projectile hit Enemy (" + enemy.getClass().getSimpleName() + "). Enemy Health: " + enemy.healthComponent.getCurrentHealth());
                    break; // Projectile hits one enemy and is destroyed
                }
            }
        }
    }

    private void checkProjectilePlayerCollisions() {
        // Player validity checked before calling this method
        if (projectileManager.getActiveProjectiles() == null) return;

        Rectangle playerBounds = player.bounds;

        for (int i = projectileManager.getActiveProjectiles().size - 1; i >= 0; i--) {
            Projectile projectile = projectileManager.getActiveProjectiles().get(i);
            // Check owner is Enemy
            if (projectile == null || !projectile.isActive() || projectile.bounds == null || !(projectile.getOwner() instanceof Enemy)) {
                continue;
            }

            Rectangle projectileBounds = projectile.bounds;

            if (Intersector.overlaps(projectileBounds, playerBounds)) {
                player.takeDamage(projectile.getDamage()); // Player handles invulnerability
                projectile.setActive(false);
                Gdx.app.debug("CollisionManager", "Enemy Projectile hit Player. Player Health: " + player.healthComponent.getCurrentHealth());
            }
        }
    }


    private boolean isMeleeAttackState(Character.State state) {
        if(state == null) return false;
        // Include all relevant melee states for both player and enemies
        return state == Character.State.LIGHT_ATTACK ||
            state == Character.State.HEAVY_ATTACK ||
            state == Character.State.ATTACK1 ||
            state == Character.State.ATTACK2 ||
            state == Character.State.ATTACK3 ||
            state == Character.State.VADERSTRIKE; // Add other melee-like specials if needed
    }

    private int getDamageForPlayerState(Character.State state) {
        if(state == null) return 0;
        switch(state) {
            case LIGHT_ATTACK: return GameConfig.LIGHT_ATTACK_DAMAGE;
            case HEAVY_ATTACK: return GameConfig.HEAVY_ATTACK_DAMAGE;
            case ATTACK3: return GameConfig.HEAVY_ATTACK_DAMAGE + 5; // Example for specific attacks
            case VADERSTRIKE: return GameConfig.HEAVY_ATTACK_DAMAGE * 2; // Example for specific attacks
            default: return 0;
        }
    }
}
