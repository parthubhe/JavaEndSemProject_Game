package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture; // Import Texture
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException; // Import
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Projectile;
import com.has.mt.interfaces.GameExceptionMessages; // Import
import com.has.mt.managers.ProjectileManager;


public class LightningMagePlayer extends Player {

    private final ProjectileManager projectileManager;

    // Projectile Details
    private static final int LIGHTNING_BALL_COLS = 9;
    private static final int LIGHTNING_BALL_ROWS = 1;
    private static final float LIGHTNING_BALL_FD = 0.1f;
    private static final int LIGHTNING_BALL_DAMAGE = GameConfig.PROJECTILE_DAMAGE + 5;


    public LightningMagePlayer(AssetLoader assetLoader, float x, float y, ProjectileManager projectileManager) {
        super(assetLoader, x, y);
        if (projectileManager == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "ProjectileManager in LightningMagePlayer");
        }
        this.projectileManager = projectileManager;
        if (this.animationComponent == null) { throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in LightningMagePlayer"); }
        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "LightningMagePlayer", e);
        }
        Gdx.app.log("LightningMagePlayer", "Created.");
    }


    @Override
    protected void setupAnimations() {
        Gdx.app.log("LightningMagePlayer", "Setting up Lightning Mage animations...");
        animationComponent.addAnimation(State.IDLE, AssetLoader.MAGE_IDLE_PATH, 7, 1, 0.15f, PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.MAGE_WALK_PATH, 7, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.MAGE_RUN_PATH, 8, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.MAGE_JUMP_PATH, 8, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.MAGE_LIGHT_ATTACK_PATH, 10, 1, 0.08f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.MAGE_HEAVY_ATTACK_PATH, 4, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.CHARGED, AssetLoader.MAGE_CHARGED_PATH, 7, 1, 0.1f, PlayMode.NORMAL); // LM_Chargeball.png
        animationComponent.addAnimation(State.VADERSTRIKE, AssetLoader.MAGE_VADER_STRIKE_PATH, 13, 1, 0.09f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.MAGE_HURT_PATH, 3, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.MAGE_DEAD_PATH, 5, 1, 0.15f, PlayMode.NORMAL);

        // --- CHANGE START: Link LIGHTNING_BALL_CAST back to IDLE or a very short anim ---
        // The actual projectile spawn happens after CHARGED finishes.
        // LIGHTNING_BALL_CAST state is now just a brief moment before returning to idle.
        if (animationComponent.hasAnimationForState(State.IDLE)) {
            animationComponent.linkStateAnimation(State.LIGHTNING_BALL_CAST, State.IDLE);
            Gdx.app.log("LightningMagePlayer", "Linked LIGHTNING_BALL_CAST state to IDLE animation (for quick transition).");
        } else {
            Gdx.app.error("LightningMagePlayer", "Cannot link LIGHTNING_BALL_CAST: IDLE animation missing.");
        }
        // --- CHANGE END ---

        // Link FALL state
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("LightningMagePlayer", "Animations set up.");
    }

    // Map inputs
    @Override
    protected State mapSpecial1ToAction() { return State.CHARGED; } // E key starts charge using LM_Chargeball.png
    @Override
    protected State mapSpecial2ToAction() { return State.VADERSTRIKE; } // V key starts LM_VaderStrike.png


    @Override
    public void updateAttack(float delta) { // Changed to public
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            // Handle state transitions and actions
            switch (currentAttackState) {
                case CHARGED: // Playing LM_Chargeball.png
                    if (animationComponent.isAnimationFinished(State.CHARGED)) {
                        // Finished charging, spawn projectile and briefly enter CAST state
                        Gdx.app.log("LightningMagePlayer", "Charged finished, spawning projectile and entering CAST state.");
                        spawnLightningBall(); // Spawn the ball
                        // --- CHANGE START: Transition to CAST state (linked to IDLE) ---
                        // This state is very short, mainly to prevent immediate re-attack
                        stateComponent.setState(State.LIGHTNING_BALL_CAST);
                        animationComponent.resetStateTimer(State.LIGHTNING_BALL_CAST);
                        // Since CAST is linked to IDLE (looping), we mark sequence complete immediately
                        attackSequenceComplete = true;
                        // --- CHANGE END ---
                    }
                    break;



                case LIGHT_ATTACK:
                case HEAVY_ATTACK:
                case VADERSTRIKE:
                    // Handle melee/special attacks finish
                    if (animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                    }
                    break;

                default:
                    // If in an unknown attack state, finish if animation ends
                    // Needed because LIGHTNING_BALL_CAST linked to IDLE won't trigger isAnimationFinished
                    if (currentAttackState != State.LIGHTNING_BALL_CAST && animationComponent.isAnimationFinished(currentAttackState)){
                        attackSequenceComplete = true;
                        Gdx.app.log("LightningMagePlayer", "Attack state " + currentAttackState + " finished (default).");
                    }
                    break;
            }

            // Reset state if any attack sequence finished this frame
            if (attackSequenceComplete) {
                Gdx.app.debug("LightningMagePlayer", "Attack/Cast Sequence Finished: " + currentAttackState);
                isAttacking = false;
                attackTimer = attackCooldown; // Apply cooldown
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                // Ensure we don't immediately exit the short CAST state if that was the finisher
                if (stateComponent.getCurrentState() != State.LIGHTNING_BALL_CAST) {
                    stateComponent.setState(nextState);
                } else {
                    // If the sequence finisher WAS the cast state, just ensure isAttacking is false
                    // and let the normal state machine take over next frame (will likely go IDLE/FALL)
                    Gdx.app.debug("LightningMagePlayer", "Transitioning out of CAST state.");
                }
            }

        } else if (attackTimer > 0) {
            attackTimer -= delta; // Tick cooldown
        }
    }

    private void spawnLightningBall() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return; // Safety checks
        Gdx.app.log("LightningMagePlayer", "Spawning Lightning Ball Projectile");
        float spawnOffsetY = bounds.height * 0.5f; // Centered vertically approx
        float projectileWidth = 0;
        try { // Safely get projectile texture width
            Texture projectileTexture = assetLoader.get(AssetLoader.MAGE_LIGHTNING_BALL_PATH, Texture.class);
            if (LIGHTNING_BALL_COLS > 0) projectileWidth = (projectileTexture.getWidth() / LIGHTNING_BALL_COLS) * 2.0f; // Scale = 2.0f
        } catch (Exception e) { Gdx.app.error("LMP", "Failed to get projectile texture for width calculation.", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.7f : position.x + bounds.width * 0.3f - projectileWidth; // Adjust origin based on direction
        float spawnY = position.y + spawnOffsetY;

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? GameConfig.PROJECTILE_SPEED : -GameConfig.PROJECTILE_SPEED, 0,
            LIGHTNING_BALL_DAMAGE, this, AssetLoader.MAGE_LIGHTNING_BALL_PATH, // Projectile uses LM_Charge.png
            LIGHTNING_BALL_COLS, LIGHTNING_BALL_ROWS, LIGHTNING_BALL_FD);
        projectileManager.addProjectile(proj);
    }
}
