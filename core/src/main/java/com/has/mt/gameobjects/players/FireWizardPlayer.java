package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture; // Added import
import com.badlogic.gdx.graphics.g2d.Animation;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Projectile;
import com.has.mt.interfaces.GameExceptionMessages;
import com.has.mt.managers.ProjectileManager;

public class FireWizardPlayer extends Player {

    private final ProjectileManager projectileManager;

    // Projectile details (VERIFY FRAME COUNTS)
    private static final int FIREBALL_COLS = 8;
    private static final int FIREBALL_ROWS = 1;
    private static final float FIREBALL_FD = 0.1f;
    private static final int FIREBALL_DAMAGE = 18;

    private static final int FLAMEJET_COLS = 14;
    private static final int FLAMEJET_ROWS = 1;
    private static final float FLAMEJET_FD = 0.08f;
    private static final int FLAMEJET_DAMAGE = 8; // Example damage


    public FireWizardPlayer(AssetLoader assetLoader, float x, float y, ProjectileManager projectileManager) {
        super(assetLoader, x, y);
        if (projectileManager == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "ProjectileManager in FireWizardPlayer");
        }
        this.projectileManager = projectileManager;

        if (this.animationComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in FireWizardPlayer");
        }

        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "FireWizardPlayer", e);
        }
        Gdx.app.log("FireWizardPlayer", "Fire Wizard Player Created.");
    }

    @Override
    protected void setupAnimations() {
        Gdx.app.log("FireWizardPlayer", "Setting up Fire Wizard animations...");
        // Frame counts based on guesses - VERIFY!
        // Idle: 8x1, Walk: 8x1, Run: 8x1, Jump: 3x1, Hurt: 3x1, Dead: 7x1
        // Attack1: 8x1, Attack2: 8x1, Charge: 6x1?, Fireball: 5x1, FlameJet: 5x1
        animationComponent.addAnimation(State.IDLE, AssetLoader.FIRE_WIZARD_IDLE_PATH, 7, 1, 0.15f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.FIRE_WIZARD_WALK_PATH, 6, 1, 0.1f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.FIRE_WIZARD_RUN_PATH, 8, 1, 0.08f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.FIRE_WIZARD_JUMP_PATH, 9, 1, 0.15f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.FIRE_WIZARD_HURT_PATH, 3, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.FIRE_WIZARD_DEAD_PATH, 7, 1, 0.15f, Animation.PlayMode.NORMAL);

        // Map Melee Attacks
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.FIRE_WIZARD_ATTACK1_PATH, 4, 1, 0.08f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.FIRE_WIZARD_ATTACK2_PATH, 4, 1, 0.09f, Animation.PlayMode.NORMAL);

        // --- CHANGE START: Use Charge.png for CAST states ---
        // Map the CAST states to the Charge.png animation
        // Assuming Charge.png has 6 frames (VERIFY!)
        if (Gdx.files.internal(AssetLoader.FIRE_WIZARD_CHARGE_PATH).exists()) {
            animationComponent.addAnimation(State.FIREBALL_CAST, AssetLoader.FIRE_WIZARD_CHARGE_PATH, 12, 1, 0.1f, Animation.PlayMode.NORMAL);
            animationComponent.linkStateAnimation(State.FLAME_JET_CAST, State.FIREBALL_CAST); // Reuse same cast anim
            Gdx.app.log("FireWizardPlayer", "Mapped CAST states to Charge animation.");
        } else {
            Gdx.app.error("FireWizardPlayer", "Charge animation not found! Linking cast states to IDLE.");
            animationComponent.linkStateAnimation(State.FIREBALL_CAST, State.IDLE);
            animationComponent.linkStateAnimation(State.FLAME_JET_CAST, State.IDLE);
        }
        // --- CHANGE END ---

        // Link Fall state
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("FireWizardPlayer", "Fire Wizard animations setup complete.");
    }

    @Override
    protected State mapSpecial1ToAction() { return State.FIREBALL_CAST; } // E key
    @Override
    protected State mapSpecial2ToAction() { return State.FLAME_JET_CAST; } // V key


    @Override
    public void updateAttack(float delta) { // Changed to public
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            // --- CHANGE START: Spawn projectile AFTER cast animation ---
            if (currentAttackState == State.FIREBALL_CAST) {
                if (animationComponent.isAnimationFinished(currentAttackState)) {
                    spawnFireball();
                    attackSequenceComplete = true;
                }
            } else if (currentAttackState == State.FLAME_JET_CAST) {
                if (animationComponent.isAnimationFinished(currentAttackState)) {
                    spawnFlameJet();
                    attackSequenceComplete = true;
                }
            }
            // --- CHANGE END ---
            // Handle standard melee attacks finish
            else if ((currentAttackState == State.LIGHT_ATTACK || currentAttackState == State.HEAVY_ATTACK) &&
                animationComponent.isAnimationFinished(currentAttackState)) {
                attackSequenceComplete = true;
            }
            // Default finish check if needed
            else if(animationComponent.isAnimationFinished(currentAttackState) &&
                currentAttackState != State.FIREBALL_CAST && currentAttackState != State.FLAME_JET_CAST && // Avoid double finish
                currentAttackState != State.LIGHT_ATTACK && currentAttackState != State.HEAVY_ATTACK) {
                attackSequenceComplete = true;
            }

            if (attackSequenceComplete) {
                Gdx.app.debug("FireWizardPlayer", "Attack/Cast Finished: " + currentAttackState);
                isAttacking = false;
                attackTimer = attackCooldown;
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    private void spawnFireball() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("FireWizardPlayer", "Spawning Fireball projectile");
        float spawnOffsetY = bounds.height * 0.5f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.FIRE_WIZARD_FIREBALL_PATH, Texture.class);
            if(FIREBALL_COLS > 0) projectileWidth = (projectileTexture.getWidth() / FIREBALL_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("FireWizardPlayer", "Failed to get fireball texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 0.9f;

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            FIREBALL_DAMAGE, this,
            AssetLoader.FIRE_WIZARD_FIREBALL_PATH, // Use Fireball.png for projectile
            FIREBALL_COLS, FIREBALL_ROWS, FIREBALL_FD);
        projectileManager.addProjectile(proj);
    }

    private void spawnFlameJet() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("FireWizardPlayer", "Spawning Flame Jet projectile");
        float spawnOffsetY = bounds.height * 0.4f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.FIRE_WIZARD_FLAME_JET_PATH, Texture.class);
            if(FLAMEJET_COLS > 0) projectileWidth = (projectileTexture.getWidth() / FLAMEJET_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("FireWizardPlayer", "Failed to get flamejet texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.7f : position.x + bounds.width * 0.3f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 1.1f;

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            FLAMEJET_DAMAGE, this,
            AssetLoader.FIRE_WIZARD_FLAME_JET_PATH, // Use Flame_jet.png for projectile
            FLAMEJET_COLS, FLAMEJET_ROWS, FLAMEJET_FD);
        projectileManager.addProjectile(proj);
    }

    // Melee damage check placeholder
    private void triggerMeleeDamage(int damage) {
        Gdx.app.debug("FireWizardPlayer", "Triggering melee damage check: " + damage);
    }
}
