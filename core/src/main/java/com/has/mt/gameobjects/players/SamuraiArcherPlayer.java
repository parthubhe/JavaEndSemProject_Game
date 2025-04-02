package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Projectile;
import com.has.mt.interfaces.GameExceptionMessages;
import com.has.mt.managers.ProjectileManager;

public class SamuraiArcherPlayer extends Player {

    private final ProjectileManager projectileManager;

    // Projectile Details (VERIFY FRAME COUNTS AND DAMAGE)
    private static final int ARROW_COLS = 1;
    private static final int ARROW_ROWS = 1;
    private static final float ARROW_FD = 0.05f; // Fast arrow animation
    private static final int ARROW_DAMAGE = GameConfig.PROJECTILE_DAMAGE + 2; // Standard arrow damage


    public SamuraiArcherPlayer(AssetLoader assetLoader, float x, float y, ProjectileManager projectileManager) {
        super(assetLoader, x, y);
        if (projectileManager == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "ProjectileManager in SamuraiArcherPlayer");
        }
        this.projectileManager = projectileManager;

        if (this.animationComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in SamuraiArcherPlayer");
        }

        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "SamuraiArcherPlayer", e);
        }
        Gdx.app.log("SamuraiArcherPlayer", "Samurai Archer Player Created.");
    }

    @Override
    protected void setupAnimations() {
        Gdx.app.log("SamuraiArcherPlayer", "Setting up Samurai Archer animations...");
        // Frame counts based on guesses - VERIFY!
        animationComponent.addAnimation(State.IDLE, AssetLoader.SAMURAI_ARCHER_IDLE_PATH, 9, 1, 0.15f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.SAMURAI_ARCHER_WALK_PATH, 8, 1, 0.1f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.SAMURAI_ARCHER_RUN_PATH, 8, 1, 0.08f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.SAMURAI_ARCHER_JUMP_PATH, 9, 1, 0.15f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.SAMURAI_ARCHER_HURT_PATH, 3, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.SAMURAI_ARCHER_DEAD_PATH, 5, 1, 0.15f, Animation.PlayMode.NORMAL);

        // Map Attacks - Use Light/Heavy for melee if desired, map Shot to Special1
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.SAMURAI_ARCHER_ATTACK1_PATH, 5, 1, 0.09f, Animation.PlayMode.NORMAL); // Melee 1
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.SAMURAI_ARCHER_ATTACK2_PATH, 5, 1, 0.1f, Animation.PlayMode.NORMAL); // Melee 2
        animationComponent.addAnimation(State.ATTACK3, AssetLoader.SAMURAI_ARCHER_ATTACK3_PATH, 6, 1, 0.1f, Animation.PlayMode.NORMAL);

        // Map ARROW_SHOT state to the "Shot" animation
        animationComponent.addAnimation(State.ARROW_SHOT, AssetLoader.SAMURAI_ARCHER_SHOT_PATH, 14, 1, 0.07f, Animation.PlayMode.NORMAL); // Cast/Shoot anim

        // Link Fall state
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("SamuraiArcherPlayer", "Samurai Archer animations setup complete.");
    }

    // Map inputs
    @Override
    protected State mapSpecial1ToAction() { return State.ARROW_SHOT; } // E key shoots arrow
    @Override
    protected State mapSpecial2ToAction() { return State.IDLE; } // V key - no default action


    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            switch(currentAttackState) {
                case ARROW_SHOT:
                    // Spawn arrow near the end of the shot animation
                    if (animationComponent.getStateTimer(currentAttackState) >= animationComponent.getAnimationDuration(currentAttackState) * 0.8f && // Check timer progress
                        !projectileHasBeenSpawnedThisAttack()) { // Add a flag/check to prevent multi-spawns per anim cycle
                        spawnArrow();
                        markProjectileSpawned(); // Set the flag
                    }
                    if (animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                        resetProjectileSpawnedFlag(); // Reset flag for next attack
                    }
                    break;
                case LIGHT_ATTACK:
                case HEAVY_ATTACK:
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                    }
                    // Add melee damage timing if needed
                    break;
                default:
                    if(animationComponent.isAnimationFinished(currentAttackState)){
                        attackSequenceComplete = true;
                    }
                    break;
            }

            if (attackSequenceComplete) {
                Gdx.app.debug("SamuraiArcherPlayer", "Attack/Cast Finished: " + currentAttackState);
                isAttacking = false;
                attackTimer = attackCooldown;
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    // --- Helper methods for projectile spawn tracking (simple example) ---
    private boolean _projectileSpawned = false;
    private boolean projectileHasBeenSpawnedThisAttack() { return _projectileSpawned; }
    private void markProjectileSpawned() { _projectileSpawned = true; }
    private void resetProjectileSpawnedFlag() { _projectileSpawned = false; }
    // -----------------------------------------------------------------


    private void spawnArrow() {
        if (projectileManager == null || position == null || bounds == null) return;
        Gdx.app.log("SamuraiArcherPlayer", "Spawning Arrow");
        float spawnOffsetY = bounds.height * 0.2f;
        float projectileWidth = (ARROW_COLS > 0 ? (assetLoader.get(AssetLoader.SAMURAI_ARCHER_ARROW_PATH, Texture.class).getWidth() / ARROW_COLS * 2.0f) : 0); // Projectile scale = 2.0f
        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 1.5f; // Fast arrow

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0, // Straight horizontal shot
            ARROW_DAMAGE, this,
            AssetLoader.SAMURAI_ARCHER_ARROW_PATH,
            ARROW_COLS, ARROW_ROWS, ARROW_FD);
        projectileManager.addProjectile(proj);
    }
}
