package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Projectile;
import com.has.mt.interfaces.GameExceptionMessages;
import com.has.mt.managers.ProjectileManager;

public class FireWizardPlayer extends Player {

    private final ProjectileManager projectileManager;

    // --- CHANGE START: Define constants for projectile anims ---
    // Projectile details for the fireball (spawned after FIREBALL_CAST)
    private static final int FIREBALL_PROJ_COLS = 12; // Fireball.png is 12x1
    private static final int FIREBALL_PROJ_ROWS = 1;
    private static final float FIREBALL_PROJ_FD = 0.1f;
    private static final int FIREBALL_PROJ_DAMAGE = 18;

    // Projectile details for the flame jet (spawned after FLAME_JET_CAST)
    private static final int FLAMEJET_PROJ_COLS = 14; // Flame_jet.png is 14x1
    private static final int FLAMEJET_PROJ_ROWS = 1;
    private static final float FLAMEJET_PROJ_FD = 0.08f;
    private static final int FLAMEJET_PROJ_DAMAGE = 8;
    // --- CHANGE END ---

    // Projectile details for the charge projectile (spawned after CHARGED)
    // Charge.png is 6x1 according to Wanderer Mage setup attempt, VERIFY THIS
    private static final int CHARGE_PROJ_COLS = 6;
    private static final int CHARGE_PROJ_ROWS = 1;
    private static final float CHARGE_PROJ_FD = 0.1f;
    private static final int CHARGE_PROJ_DAMAGE = 20; // Example damage


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
        // Standard animations
        animationComponent.addAnimation(State.IDLE, AssetLoader.FIRE_WIZARD_IDLE_PATH, 7, 1, 0.15f, PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.FIRE_WIZARD_WALK_PATH, 6, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.FIRE_WIZARD_RUN_PATH, 8, 1, 0.08f, PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.FIRE_WIZARD_JUMP_PATH, 9, 1, 0.15f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.FIRE_WIZARD_HURT_PATH, 3, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.FIRE_WIZARD_DEAD_PATH, 7, 1, 0.15f, PlayMode.NORMAL);

        // Melee attacks
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.FIRE_WIZARD_ATTACK1_PATH, 4, 1, 0.08f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.FIRE_WIZARD_ATTACK2_PATH, 4, 1, 0.09f, PlayMode.NORMAL);

        // --- CHANGE START: Map CAST states to corresponding animations ---
        // CHARGED state uses Charge.png animation
        // VERIFY frame count (6?) and duration (0.1f?)
        if (Gdx.files.internal(AssetLoader.FIRE_WIZARD_CHARGE_PATH).exists()) {
            animationComponent.addAnimation(State.CHARGED, AssetLoader.FIRE_WIZARD_CHARGE_PATH, 6, 1, 0.1f, PlayMode.NORMAL);
            Gdx.app.log("FireWizardPlayer", "Mapped CHARGED state to Charge animation.");
        } else {
            Gdx.app.error("FireWizardPlayer", "Charge animation not found at " + AssetLoader.FIRE_WIZARD_CHARGE_PATH + "! Linking CHARGED to IDLE.");
            animationComponent.linkStateAnimation(State.CHARGED, State.IDLE);
        }

        // FIREBALL_CAST state uses Fireball.png animation
        // VERIFY frame count (12?) and duration (0.1f?)
        if (Gdx.files.internal(AssetLoader.FIRE_WIZARD_FIREBALL_PATH).exists()) {
            animationComponent.addAnimation(State.FIREBALL_CAST, AssetLoader.FIRE_WIZARD_FIREBALL_PATH, 12, 1, 0.1f, PlayMode.NORMAL);
            Gdx.app.log("FireWizardPlayer", "Mapped FIREBALL_CAST state to Fireball animation.");
        } else {
            Gdx.app.error("FireWizardPlayer", "Fireball animation not found at " + AssetLoader.FIRE_WIZARD_FIREBALL_PATH + "! Linking FIREBALL_CAST to IDLE.");
            animationComponent.linkStateAnimation(State.FIREBALL_CAST, State.IDLE);
        }

        // FLAME_JET_CAST state uses Flame_jet.png animation
        // VERIFY frame count (14?) and duration (0.08f?)
        if (Gdx.files.internal(AssetLoader.FIRE_WIZARD_FLAME_JET_PATH).exists()) {
            animationComponent.addAnimation(State.FLAME_JET_CAST, AssetLoader.FIRE_WIZARD_FLAME_JET_PATH, 14, 1, 0.08f, PlayMode.NORMAL);
            Gdx.app.log("FireWizardPlayer", "Mapped FLAME_JET_CAST state to Flame_jet animation.");
        } else {
            Gdx.app.error("FireWizardPlayer", "Flame_jet animation not found at " + AssetLoader.FIRE_WIZARD_FLAME_JET_PATH + "! Linking FLAME_JET_CAST to IDLE.");
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

    // Key mapping:
    // Special1 (E key) triggers CHARGED state.
    // Special2 (V key) triggers FLAME_JET_CAST state.
    @Override
    protected State mapSpecial1ToAction() { return State.CHARGED; }
    @Override
    protected State mapSpecial2ToAction() { return State.FLAME_JET_CAST; }


    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            switch(currentAttackState) {
                case CHARGED: // Playing Charge.png
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        spawnChargeProjectile(); // Spawn projectile using Charge.png texture
                        attackSequenceComplete = true;
                    }
                    break;
                case FIREBALL_CAST: // Playing Fireball.png
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        spawnFireballProjectile(); // Spawn projectile using Fireball.png texture
                        attackSequenceComplete = true;
                    }
                    break;
                case FLAME_JET_CAST: // Playing Flame_jet.png
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        spawnFlameJetProjectile(); // Spawn projectile using Flame_jet.png texture
                        attackSequenceComplete = true;
                    }
                    break;
                case LIGHT_ATTACK:
                case HEAVY_ATTACK:
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                    }
                    break;
                default:
                    if(animationComponent.isAnimationFinished(currentAttackState)){
                        attackSequenceComplete = true;
                    }
                    break;
            }

            if (attackSequenceComplete) {
                Gdx.app.debug("FireWizardPlayer", "Attack/Cast Finished: " + currentAttackState);
                isAttacking = false;
                attackTimer = attackCooldown;
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
                // --- Minor Change: Clear hit enemy set when attack sequence finishes ---
                hitEnemiesThisAttack.clear();
                // --- End Minor Change ---
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    // Spawns projectile after CHARGED animation
    private void spawnChargeProjectile() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("FireWizardPlayer", "Spawning Charge Projectile");
        float spawnOffsetY = bounds.height * 0.5f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.FIRE_WIZARD_CHARGE_PATH, Texture.class); // Use Charge.png for projectile visual
            if(CHARGE_PROJ_COLS > 0) projectileWidth = (projectileTexture.getWidth() / CHARGE_PROJ_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("FireWizardPlayer", "Failed to get charge texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? GameConfig.PROJECTILE_SPEED * 0.9f : -GameConfig.PROJECTILE_SPEED * 0.9f, 0, // Slightly slower?
            CHARGE_PROJ_DAMAGE, this,
            AssetLoader.FIRE_WIZARD_CHARGE_PATH, // Projectile uses Charge.png
            CHARGE_PROJ_COLS, CHARGE_PROJ_ROWS, CHARGE_PROJ_FD);
        projectileManager.addProjectile(proj);
    }

    // Spawns projectile after FIREBALL_CAST animation
    private void spawnFireballProjectile() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("FireWizardPlayer", "Spawning Fireball projectile");
        float spawnOffsetY = bounds.height * 0.5f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.FIRE_WIZARD_FIREBALL_PATH, Texture.class); // Use Fireball.png for projectile visual
            if(FIREBALL_PROJ_COLS > 0) projectileWidth = (projectileTexture.getWidth() / FIREBALL_PROJ_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("FireWizardPlayer", "Failed to get fireball texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? GameConfig.PROJECTILE_SPEED : -GameConfig.PROJECTILE_SPEED, 0,
            FIREBALL_PROJ_DAMAGE, this,
            AssetLoader.FIRE_WIZARD_FIREBALL_PATH, // Projectile uses Fireball.png
            FIREBALL_PROJ_COLS, FIREBALL_PROJ_ROWS, FIREBALL_PROJ_FD);
        projectileManager.addProjectile(proj);
    }

    // Spawns projectile after FLAME_JET_CAST animation
    private void spawnFlameJetProjectile() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("FireWizardPlayer", "Spawning Flame Jet projectile");
        float spawnOffsetY = bounds.height * 0.4f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.FIRE_WIZARD_FLAME_JET_PATH, Texture.class); // Use Flame_jet.png for projectile visual
            if(FLAMEJET_PROJ_COLS > 0) projectileWidth = (projectileTexture.getWidth() / FLAMEJET_PROJ_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("FireWizardPlayer", "Failed to get flamejet texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.7f : position.x + bounds.width * 0.3f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? GameConfig.PROJECTILE_SPEED * 1.1f : -GameConfig.PROJECTILE_SPEED * 1.1f, 0, // Slightly faster?
            FLAMEJET_PROJ_DAMAGE, this,
            AssetLoader.FIRE_WIZARD_FLAME_JET_PATH, // Projectile uses Flame_jet.png
            FLAMEJET_PROJ_COLS, FLAMEJET_PROJ_ROWS, FLAMEJET_PROJ_FD);
        projectileManager.addProjectile(proj);
    }
}
