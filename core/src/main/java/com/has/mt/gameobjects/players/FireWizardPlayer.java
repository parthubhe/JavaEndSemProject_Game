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

    // Projectile details for the charge projectile (only charge spawns a projectile)
    private static final int CHARGE_PROJ_COLS = 12;
    private static final int CHARGE_PROJ_ROWS = 1;
    private static final float CHARGE_PROJ_FD = 0.1f;
    private static final int CHARGE_PROJ_DAMAGE = 18;

    public FireWizardPlayer(AssetLoader assetLoader, float x, float y, ProjectileManager projectileManager) {
        super(assetLoader, x, y);
        if (projectileManager == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "ProjectileManager in FireWizardPlayer");
        }
        this.projectileManager = projectileManager;
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

        // Melee attack animations remain unchanged
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.FIRE_WIZARD_ATTACK1_PATH, 4, 1, 0.08f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.FIRE_WIZARD_ATTACK2_PATH, 4, 1, 0.09f, PlayMode.NORMAL);

        // Load the charge animation (this is the only state that will spawn a projectile)
        animationComponent.addAnimation(State.CHARGED, AssetLoader.FIRE_WIZARD_CHARGE_PATH, 6, 1, 0.1f, PlayMode.NORMAL);
        Gdx.app.log("FireWizardPlayer", "Loaded CHARGED animation from charge.png.");

        // The following cast animations are purely cosmetic (they do not spawn projectiles)
        animationComponent.addAnimation(State.FIREBALL_CAST, AssetLoader.FIRE_WIZARD_FIREBALL_PATH, 12, 1, 0.1f, PlayMode.NORMAL);
        Gdx.app.log("FireWizardPlayer", "Loaded FIREBALL_CAST animation from fireball sprite sheet.");

        animationComponent.addAnimation(State.FLAME_JET_CAST, AssetLoader.FIRE_WIZARD_FLAME_JET_PATH, 14, 1, 0.08f, PlayMode.NORMAL);
        Gdx.app.log("FireWizardPlayer", "Loaded FLAME_JET_CAST animation from flame jet sprite sheet.");

        // Link FALL state to use the JUMP animation
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("FireWizardPlayer", "Fire Wizard animations setup complete.");
    }

    // Key mapping:
    // Special1 (E key) now triggers CHARGED (which spawns the projectile when finished).
    // Special2 (V key) triggers FLAME_JET_CAST (cosmetic only).
    @Override
    protected State mapSpecial1ToAction() {
        return State.CHARGED;
    }

    @Override
    protected State mapSpecial2ToAction() {
        return State.FLAME_JET_CAST;
    }

    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;
            switch (currentAttackState) {
                case CHARGED:
                    if (animationComponent.isAnimationFinished(State.CHARGED)) {
                        spawnChargeProjectile();
                        attackSequenceComplete = true;
                    }
                    break;
                case FIREBALL_CAST:
                case FLAME_JET_CAST:
                case LIGHT_ATTACK:
                case HEAVY_ATTACK:
                    if (animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                    }
                    break;
                default:
                    if (animationComponent.isAnimationFinished(currentAttackState)) {
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
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    // This method spawns the projectile for the CHARGED state.
    private void spawnChargeProjectile() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("FireWizardPlayer", "Spawning Charge Projectile");
        float spawnOffsetY = bounds.height * 0.5f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.FIRE_WIZARD_CHARGE_PATH, Texture.class);
            if (CHARGE_PROJ_COLS > 0)
                projectileWidth = (projectileTexture.getWidth() / CHARGE_PROJ_COLS) * 2.0f;
        } catch (Exception e) {
            Gdx.app.error("FireWizardPlayer", "Failed to get charge texture for width calculation.", e);
        }
        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? GameConfig.PROJECTILE_SPEED : -GameConfig.PROJECTILE_SPEED, 0,
            CHARGE_PROJ_DAMAGE, this, AssetLoader.FIRE_WIZARD_CHARGE_PATH,
            CHARGE_PROJ_COLS, CHARGE_PROJ_ROWS, CHARGE_PROJ_FD);
        projectileManager.addProjectile(proj);
    }
}
