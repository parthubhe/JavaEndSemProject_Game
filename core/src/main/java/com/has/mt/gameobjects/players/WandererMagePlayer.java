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

public class WandererMagePlayer extends Player {

    private final ProjectileManager projectileManager;

    // Projectile details for Magic Arrow (which will throw two charge projectiles)
    private static final int MAGIC_ARROW_COLS = 6;
    private static final int MAGIC_ARROW_ROWS = 1;
    private static final float MAGIC_ARROW_FD = 0.1f;
    private static final int MAGIC_ARROW_DAMAGE = GameConfig.PROJECTILE_DAMAGE;

    // For Magic Sphere – this is a full–body animation (no projectile spawn)
    private static final int MAGIC_SPHERE_COLS = 8;
    private static final int MAGIC_SPHERE_ROWS = 1;
    private static final float MAGIC_SPHERE_FD = 0.12f;
    // Damage value not needed as no projectile is spawned

    public WandererMagePlayer(AssetLoader assetLoader, float x, float y, ProjectileManager projectileManager) {
        super(assetLoader, x, y);
        if (projectileManager == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "ProjectileManager in WandererMagePlayer");
        }
        this.projectileManager = projectileManager;
        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "WandererMagePlayer", e);
        }
        Gdx.app.log("WandererMagePlayer", "Wanderer Mage Player Created.");
    }

    @Override
    protected void setupAnimations() {
        Gdx.app.log("WandererMagePlayer", "Setting up Wanderer Mage animations...");
        // Standard animations
        animationComponent.addAnimation(State.IDLE, AssetLoader.WANDERER_MAGE_IDLE_PATH, 8, 1, 0.15f, PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.WANDERER_MAGE_WALK_PATH, 7, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.WANDERER_MAGE_RUN_PATH, 8, 1, 0.08f, PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.WANDERER_MAGE_JUMP_PATH, 8, 1, 0.15f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.WANDERER_MAGE_HURT_PATH, 4, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.WANDERER_MAGE_DEAD_PATH, 4, 1, 0.15f, PlayMode.NORMAL);
        // Melee attacks remain the same
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.WANDERER_MAGE_ATTACK1_PATH, 7, 1, 0.08f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.WANDERER_MAGE_ATTACK2_PATH, 9, 1, 0.09f, PlayMode.NORMAL);

        // --- UPDATED: Charge/Cast animations mapping ---
        // Map the Magic Arrow cast (which spawns the charge projectiles) to be triggered by E and V keys.
        if (Gdx.files.internal(AssetLoader.WANDERER_MAGE_CHARGE1_PATH).exists()) {
            animationComponent.addAnimation(State.MAGIC_ARROW_CAST, AssetLoader.WANDERER_MAGE_CHARGE1_PATH, 9, 1, 0.09f, PlayMode.NORMAL);
            Gdx.app.log("WandererMagePlayer", "Mapped MAGIC_ARROW_CAST state to Charge_1 animation.");
        } else {
            Gdx.app.error("WandererMagePlayer", "Charge_1 animation not found! Linking MAGIC_ARROW_CAST to IDLE.");
            animationComponent.linkStateAnimation(State.MAGIC_ARROW_CAST, State.IDLE);
        }
        // Map the Magic Sphere cast (a full–body animation, no projectile) to be triggered by Q.
        if (Gdx.files.internal(AssetLoader.WANDERER_MAGE_CHARGE2_PATH).exists()) {
            animationComponent.addAnimation(State.MAGIC_SPHERE_CAST, AssetLoader.WANDERER_MAGE_CHARGE2_PATH, 6, 1, 0.12f, PlayMode.NORMAL);
            Gdx.app.log("WandererMagePlayer", "Mapped MAGIC_SPHERE_CAST state to Charge_2 animation.");
        } else {
            Gdx.app.error("WandererMagePlayer", "Charge_2 animation not found! Linking MAGIC_SPHERE_CAST to IDLE.");
            animationComponent.linkStateAnimation(State.MAGIC_SPHERE_CAST, State.IDLE);
        }

        // Link FALL state
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("WandererMagePlayer", "Wanderer Mage animations setup complete.");
    }

    // --- UPDATED: Remapped special actions ---
    // Now the magic sphere (full–body animation) is triggered via the Q key,
    // and the magic arrow cast (which spawns two charge projectiles) is triggered via E and V keys.
    // The input system should now be configured so that:
    // • Q triggers mapSpecial1ToAction (returning MAGIC_SPHERE_CAST)
    // • E and V both trigger magic arrow casts.
    @Override
    protected State mapSpecial1ToAction() {
        // Map Q key to magic sphere cast (full–body animation; no projectile spawn)
        return State.MAGIC_SPHERE_CAST;
    }
    @Override
    protected State mapSpecial2ToAction() {
        // Map one of the arrow casts (e.g., E or V) to magic arrow cast which will decide which charge projectile to spawn
        return State.MAGIC_ARROW_CAST;
    }

    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            switch(currentAttackState) {
                case MAGIC_SPHERE_CAST:
                    // For magic sphere, simply play the full–body animation with no projectile spawn.
                    if (animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                    }
                    break;
                case MAGIC_ARROW_CAST:
                    // For magic arrow cast, spawn two different charge projectiles depending on input.
                    // (Assuming that the input system distinguishes between E and V and sets an internal flag accordingly)
                    if (animationComponent.isAnimationFinished(currentAttackState)) {
                        // Here you would check which key was pressed (E or V) and call spawnMagicArrow1() or spawnMagicArrow2()
                        // For demonstration we call the same spawn method.
                        spawnMagicArrow();
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
                    if(animationComponent.isAnimationFinished(currentAttackState) &&
                        currentAttackState != State.MAGIC_SPHERE_CAST && currentAttackState != State.MAGIC_ARROW_CAST) {
                        attackSequenceComplete = true;
                    }
                    break;
            }

            if (attackSequenceComplete) {
                Gdx.app.debug("WandererMagePlayer", "Attack/Cast Finished: " + currentAttackState);
                isAttacking = false;
                attackTimer = attackCooldown;
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    private void spawnMagicArrow() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("WandererMagePlayer", "Spawning Magic Arrow projectile");
        float spawnOffsetY = bounds.height * 0.2f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.WANDERER_MAGE_MAGIC_ARROW_PATH, Texture.class);
            if (MAGIC_ARROW_COLS > 0)
                projectileWidth = (projectileTexture.getWidth() / MAGIC_ARROW_COLS) * 2.0f;
        } catch (Exception e) {
            Gdx.app.error("WandererMagePlayer", "Failed to get arrow texture for width calc", e);
        }
        float spawnX = facingRight ? position.x + bounds.width * 0.7f : position.x + bounds.width * 0.3f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 1.2f;
        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            MAGIC_ARROW_DAMAGE, this,
            AssetLoader.WANDERER_MAGE_MAGIC_ARROW_PATH, MAGIC_ARROW_COLS, MAGIC_ARROW_ROWS, MAGIC_ARROW_FD);
        projectileManager.addProjectile(proj);
    }

    // If needed, separate methods (spawnMagicArrow1 and spawnMagicArrow2) can be defined here
    // to differentiate between the two charge projectiles thrown when E and V are pressed.
}
