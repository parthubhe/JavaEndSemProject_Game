// ######## START OF FILE: gameobjects/players/WandererMagePlayer.java ########
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

    // --- SWAPPED CONSTANTS FOR PROJECTILES ---

    // Projectile details for the projectile spawned after MAGIC_SPHERE_CAST (E Key)
    // Uses Charge_1.png texture (as requested)
    // VERIFY frame count (9?) and duration (0.09f?) for Charge_1.png
    private static final int CHARGE1_PROJ_COLS = 9;
    private static final int CHARGE1_PROJ_ROWS = 1;
    private static final float CHARGE1_PROJ_FD = 0.09f;
    private static final int CHARGE1_PROJ_DAMAGE = GameConfig.PROJECTILE_DAMAGE + 8; // Damage associated with Sphere Cast (E key)

    // Projectile details for the projectile spawned after MAGIC_ARROW_CAST (V Key)
    // Uses Charge_2.png texture (as requested)
    // VERIFY frame count (6?) and duration (0.12f?) for Charge_2.png
    private static final int CHARGE2_PROJ_COLS = 6;
    private static final int CHARGE2_PROJ_ROWS = 1;
    private static final float CHARGE2_PROJ_FD = 0.12f;
    private static final int CHARGE2_PROJ_DAMAGE = GameConfig.PROJECTILE_DAMAGE; // Damage associated with Arrow Cast (V key)
    // --- END SWAPPED CONSTANTS ---


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
        // Standard animations
        animationComponent.addAnimation(State.IDLE, AssetLoader.WANDERER_MAGE_IDLE_PATH, 8, 1, 0.15f, PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.WANDERER_MAGE_WALK_PATH, 7, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.WANDERER_MAGE_RUN_PATH, 8, 1, 0.08f, PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.WANDERER_MAGE_JUMP_PATH, 8, 1, 0.15f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.WANDERER_MAGE_HURT_PATH, 4, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.WANDERER_MAGE_DEAD_PATH, 4, 1, 0.15f, PlayMode.NORMAL);

        // Melee attacks
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.WANDERER_MAGE_ATTACK1_PATH, 7, 1, 0.08f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.WANDERER_MAGE_ATTACK2_PATH, 9, 1, 0.09f, PlayMode.NORMAL);

        // Load CAST animations (Magic_arrow.png and Magic_sphere.png)
        if (Gdx.files.internal(AssetLoader.WANDERER_MAGE_MAGIC_ARROW_PATH).exists()) {
            animationComponent.addAnimation(State.MAGIC_ARROW_CAST, AssetLoader.WANDERER_MAGE_MAGIC_ARROW_PATH, 6, 1, 0.1f, PlayMode.NORMAL);
            Gdx.app.log("WandererMagePlayer", "Mapped MAGIC_ARROW_CAST state (V Key) to Magic_arrow animation.");
        } else {
            Gdx.app.error("WandererMagePlayer", "Magic_arrow animation not found! Linking MAGIC_ARROW_CAST to IDLE.");
            animationComponent.linkStateAnimation(State.MAGIC_ARROW_CAST, State.IDLE);
        }
        // MAGIC_SPHERE_CAST state (E Key) uses Magic_sphere.png animation
        if (Gdx.files.internal(AssetLoader.WANDERER_MAGE_MAGIC_SPHERE_PATH).exists()) {
            // Adjusted frame count based on user feedback in previous message
            animationComponent.addAnimation(State.MAGIC_SPHERE_CAST, AssetLoader.WANDERER_MAGE_MAGIC_SPHERE_PATH, 16, 1, 0.12f, PlayMode.NORMAL); // Was 16, correcting to 8 based on asset list
            Gdx.app.log("WandererMagePlayer", "Mapped MAGIC_SPHERE_CAST state (E Key) to Magic_sphere animation.");
        } else {
            Gdx.app.error("WandererMagePlayer", "Magic_sphere animation not found! Linking MAGIC_SPHERE_CAST to IDLE.");
            animationComponent.linkStateAnimation(State.MAGIC_SPHERE_CAST, State.IDLE);
        }

        // Link Fall state
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("WandererMagePlayer", "Wanderer Mage animations setup complete.");
    }

    // Map inputs to specific CAST states
    @Override
    protected State mapSpecial1ToAction() { return State.MAGIC_SPHERE_CAST; } // E key -> plays Magic_sphere.png -> spawns Charge_1.png projectile
    @Override
    protected State mapSpecial2ToAction() { return State.MAGIC_ARROW_CAST; } // V key -> plays Magic_arrow.png -> spawns Charge_2.png projectile

    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            switch(currentAttackState) {
                // --- SWAPPED SPAWN CALLS ---
                case MAGIC_SPHERE_CAST: // Playing Magic_sphere.png (from E key)
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        spawnCharge1Projectile(); // Spawn projectile using Charge_1.png texture
                        attackSequenceComplete = true;
                    }
                    break;
                case MAGIC_ARROW_CAST: // Playing Magic_arrow.png (from V key)
                    if(animationComponent.isAnimationFinished(currentAttackState)) {
                        spawnCharge2Projectile(); // Spawn projectile using Charge_2.png texture
                        attackSequenceComplete = true;
                    }
                    break;
                // --- END SWAPPED SPAWN CALLS ---

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
                Gdx.app.debug("WandererMagePlayer", "Attack/Cast Sequence Finished: " + currentAttackState);
                isAttacking = false;
                attackTimer = attackCooldown;
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
                hitEnemiesThisAttack.clear();
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    // Spawns projectile using Charge_1.png texture (after MAGIC_SPHERE_CAST finishes - E Key)
    private void spawnCharge1Projectile() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("WandererMagePlayer", "Spawning Charge_1 projectile (after Magic Sphere cast)");
        float spawnOffsetY = bounds.height * 0.1f; // Adjust as needed
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.WANDERER_MAGE_CHARGE1_PATH, Texture.class);
            if(CHARGE1_PROJ_COLS > 0) projectileWidth = (projectileTexture.getWidth() / CHARGE1_PROJ_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("WandererMagePlayer", "Failed to get Charge_1 texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth; // Adjust origin
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 0.8f; // Sphere speed

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            CHARGE1_PROJ_DAMAGE, this, // Use damage associated with the sphere cast (E key)
            AssetLoader.WANDERER_MAGE_CHARGE1_PATH, // Projectile uses Charge_1.png
            CHARGE1_PROJ_COLS, CHARGE1_PROJ_ROWS, CHARGE1_PROJ_FD);
        projectileManager.addProjectile(proj);
    }

    // Spawns projectile using Charge_2.png texture (after MAGIC_ARROW_CAST finishes - V Key)
    private void spawnCharge2Projectile() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("WandererMagePlayer", "Spawning Charge_2 projectile (after Magic Arrow cast)");
        float spawnOffsetY = bounds.height * 0.1f; // Adjust as needed
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.WANDERER_MAGE_CHARGE2_PATH, Texture.class);
            if(CHARGE2_PROJ_COLS > 0) projectileWidth = (projectileTexture.getWidth() / CHARGE2_PROJ_COLS) * 2.0f;
        } catch (Exception e) { Gdx.app.error("WandererMagePlayer", "Failed to get Charge_2 texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.7f : position.x + bounds.width * 0.3f - projectileWidth; // Adjust origin
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 1.2f; // Arrow speed

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            CHARGE2_PROJ_DAMAGE, this, // Use damage associated with the arrow cast (V key)
            AssetLoader.WANDERER_MAGE_CHARGE2_PATH, // Projectile uses Charge_2.png
            CHARGE2_PROJ_COLS, CHARGE2_PROJ_ROWS, CHARGE2_PROJ_FD);
        projectileManager.addProjectile(proj);
    }
}
// ######## END OF FILE: gameobjects/players/WandererMagePlayer.java ########
