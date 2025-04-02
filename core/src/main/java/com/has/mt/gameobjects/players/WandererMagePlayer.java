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

public class WandererMagePlayer extends Player {

    private final ProjectileManager projectileManager;

    // Projectile Details (VERIFY FRAME COUNTS AND DAMAGE)
    private static final int MAGIC_ARROW_COLS = 6;
    private static final int MAGIC_ARROW_ROWS = 1;
    private static final float MAGIC_ARROW_FD = 0.1f;
    private static final int MAGIC_ARROW_DAMAGE = GameConfig.PROJECTILE_DAMAGE;

    private static final int MAGIC_SPHERE_COLS = 8; // Magic_sphere.png is 8x1 according to asset list
    private static final int MAGIC_SPHERE_ROWS = 1;
    private static final float MAGIC_SPHERE_FD = 0.12f;
    private static final int MAGIC_SPHERE_DAMAGE = GameConfig.PROJECTILE_DAMAGE + 8;


    public WandererMagePlayer(AssetLoader assetLoader, float x, float y, ProjectileManager projectileManager) {
        super(assetLoader, x, y);
        if (projectileManager == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "ProjectileManager in WandererMagePlayer");
        }
        this.projectileManager = projectileManager;

        if (this.animationComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in WandererMagePlayer");
        }

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
        // Frame counts based on asset list - VERIFY!
        // Idle: 8x1, Walk: 7x1, Run: 8x1, Jump: 8x1, Hurt: 4x1, Dead: 4x1
        // Attack1: 7x1, Attack2: 9x1, Charge1: 9x1, Charge2: 6x1
        // MagicArrow: 6x1 (Projectile), MagicSphere: 8x1 (Projectile)
        animationComponent.addAnimation(State.IDLE, AssetLoader.WANDERER_MAGE_IDLE_PATH, 8, 1, 0.15f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.WANDERER_MAGE_WALK_PATH, 7, 1, 0.1f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.WANDERER_MAGE_RUN_PATH, 8, 1, 0.08f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.WANDERER_MAGE_JUMP_PATH, 8, 1, 0.15f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.WANDERER_MAGE_HURT_PATH, 4, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.WANDERER_MAGE_DEAD_PATH, 4, 1, 0.15f, Animation.PlayMode.NORMAL);

        // Map Melee Attacks
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.WANDERER_MAGE_ATTACK1_PATH, 7, 1, 0.08f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.WANDERER_MAGE_ATTACK2_PATH, 9, 1, 0.09f, Animation.PlayMode.NORMAL);

        // --- CHANGE START: Use Charge animations for CAST states ---
        // Map MAGIC_ARROW_CAST state to Charge_1.png animation
        if (Gdx.files.internal(AssetLoader.WANDERER_MAGE_CHARGE1_PATH).exists()) {
            animationComponent.addAnimation(State.MAGIC_ARROW_CAST, AssetLoader.WANDERER_MAGE_CHARGE1_PATH, 9, 1, 0.09f, Animation.PlayMode.NORMAL);
            Gdx.app.log("WandererMagePlayer", "Mapped MAGIC_ARROW_CAST state to Charge_1 animation.");
        } else {
            Gdx.app.error("WandererMagePlayer", "Charge_1 animation not found! Linking MAGIC_ARROW_CAST to IDLE.");
            animationComponent.linkStateAnimation(State.MAGIC_ARROW_CAST, State.IDLE);
        }

        // Map MAGIC_SPHERE_CAST state to Charge_2.png animation
        if (Gdx.files.internal(AssetLoader.WANDERER_MAGE_CHARGE2_PATH).exists()) {
            animationComponent.addAnimation(State.MAGIC_SPHERE_CAST, AssetLoader.WANDERER_MAGE_CHARGE2_PATH, 6, 1, 0.12f, Animation.PlayMode.NORMAL);
            Gdx.app.log("WandererMagePlayer", "Mapped MAGIC_SPHERE_CAST state to Charge_2 animation.");
        } else {
            Gdx.app.error("WandererMagePlayer", "Charge_2 animation not found! Linking MAGIC_SPHERE_CAST to IDLE.");
            animationComponent.linkStateAnimation(State.MAGIC_SPHERE_CAST, State.IDLE);
        }
        // --- CHANGE END ---

        // Link Fall state
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("WandererMagePlayer", "Wanderer Mage animations setup complete.");
    }

    // Map inputs to specific cast states
    @Override
    protected State mapSpecial1ToAction() { return State.MAGIC_ARROW_CAST; } // E key
    @Override
    protected State mapSpecial2ToAction() { return State.MAGIC_SPHERE_CAST; } // V key


    @Override
    public void updateAttack(float delta) { // Changed to public
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            switch(currentAttackState) {
                case MAGIC_ARROW_CAST: // Playing Charge_1.png
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        spawnMagicArrow(); // Spawn projectile using Magic_arrow.png
                        attackSequenceComplete = true;
                    }
                    break;
                case MAGIC_SPHERE_CAST: // Playing Charge_2.png
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        spawnMagicSphere(); // Spawn projectile using Magic_sphere.png
                        attackSequenceComplete = true;
                    }
                    break;
                case LIGHT_ATTACK: // Playing Attack_1.png
                case HEAVY_ATTACK: // Playing Attack_2.png
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                    }
                    // Add melee damage timing if needed
                    break;
                default:
                    // Finish if animation ends for any other attack state
                    if(animationComponent.isAnimationFinished(currentAttackState) &&
                        currentAttackState != State.MAGIC_ARROW_CAST && // Avoid double finish checks
                        currentAttackState != State.MAGIC_SPHERE_CAST){
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
        float spawnOffsetY = bounds.height * 0.6f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.WANDERER_MAGE_MAGIC_ARROW_PATH, Texture.class);
            if(MAGIC_ARROW_COLS > 0) projectileWidth = (projectileTexture.getWidth() / MAGIC_ARROW_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("WandererMagePlayer", "Failed to get arrow texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.7f : position.x + bounds.width * 0.3f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 1.2f;

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            MAGIC_ARROW_DAMAGE, this,
            AssetLoader.WANDERER_MAGE_MAGIC_ARROW_PATH, // Use Magic_arrow.png for projectile
            MAGIC_ARROW_COLS, MAGIC_ARROW_ROWS, MAGIC_ARROW_FD);
        projectileManager.addProjectile(proj);
    }

    private void spawnMagicSphere() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("WandererMagePlayer", "Spawning Magic Sphere projectile");
        float spawnOffsetY = bounds.height * 0.5f;
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.WANDERER_MAGE_MAGIC_SPHERE_PATH, Texture.class);
            if(MAGIC_SPHERE_COLS > 0) projectileWidth = (projectileTexture.getWidth() / MAGIC_SPHERE_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("WandererMagePlayer", "Failed to get sphere texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 0.8f;

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            MAGIC_SPHERE_DAMAGE, this,
            AssetLoader.WANDERER_MAGE_MAGIC_SPHERE_PATH, // Use Magic_sphere.png for projectile
            MAGIC_SPHERE_COLS, MAGIC_SPHERE_ROWS, MAGIC_SPHERE_FD);
        projectileManager.addProjectile(proj);
    }
}
