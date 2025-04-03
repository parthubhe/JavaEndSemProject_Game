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

    // Projectile details
    private static final int ARROW_PROJ_COLS = 1; // Arrow.png is 1x1? VERIFY
    private static final int ARROW_PROJ_ROWS = 1;
    private static final float ARROW_PROJ_FD = 0.05f;
    private static final int ARROW_PROJ_DAMAGE = GameConfig.PROJECTILE_DAMAGE + 2;

    // Helper for projectile spawn tracking per attack
    private boolean projectileSpawnedThisAttack = false;


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
        // VERIFY Frame counts!
        animationComponent.addAnimation(State.IDLE, AssetLoader.SAMURAI_ARCHER_IDLE_PATH, 9, 1, 0.15f, Animation.PlayMode.LOOP); // 9 frames?
        animationComponent.addAnimation(State.WALK, AssetLoader.SAMURAI_ARCHER_WALK_PATH, 8, 1, 0.1f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.SAMURAI_ARCHER_RUN_PATH, 8, 1, 0.08f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.SAMURAI_ARCHER_JUMP_PATH, 9, 1, 0.15f, Animation.PlayMode.NORMAL); // 9 frames?
        animationComponent.addAnimation(State.HURT, AssetLoader.SAMURAI_ARCHER_HURT_PATH, 3, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.SAMURAI_ARCHER_DEAD_PATH, 5, 1, 0.15f, Animation.PlayMode.NORMAL); // 5 frames?
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.SAMURAI_ARCHER_ATTACK1_PATH, 5, 1, 0.09f, Animation.PlayMode.NORMAL); // 5 frames?
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.SAMURAI_ARCHER_ATTACK2_PATH, 5, 1, 0.1f, Animation.PlayMode.NORMAL); // 5 frames?
        animationComponent.addAnimation(State.ATTACK3, AssetLoader.SAMURAI_ARCHER_ATTACK3_PATH, 6, 1, 0.1f, Animation.PlayMode.NORMAL); // 6 frames?
        animationComponent.addAnimation(State.ARROW_SHOT, AssetLoader.SAMURAI_ARCHER_SHOT_PATH, 14, 1, 0.07f, Animation.PlayMode.NORMAL); // 14 frames?

        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("SamuraiArcherPlayer", "Samurai Archer animations setup complete.");
    }

    @Override
    protected State mapSpecial1ToAction() { return State.ARROW_SHOT; }
    @Override
    protected State mapSpecial2ToAction() { return State.IDLE; }

    @Override
    public void startAttack(State attackState) {
        super.startAttack(attackState);
        if (attackState == State.ARROW_SHOT) {
            projectileSpawnedThisAttack = false; // Reset flag when arrow shot starts
        }
    }

    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            switch(currentAttackState) {
                case ARROW_SHOT:
                    // Spawn arrow near the end of the shot animation, only once
                    float animDuration = animationComponent.getAnimationDuration(currentAttackState);
                    float currentTime = animationComponent.getStateTimer(currentAttackState);
                    if (animDuration > 0 && currentTime >= animDuration * 0.8f && !projectileSpawnedThisAttack) {
                        spawnArrowProjectile();
                        projectileSpawnedThisAttack = true; // Mark as spawned
                    }
                    if (animationComponent.isAnimationFinished(currentAttackState)) {
                        attackSequenceComplete = true;
                    }
                    break;
                case LIGHT_ATTACK:
                case HEAVY_ATTACK:
                case ATTACK3: // Included ATTACK3 if it's a melee attack
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
                Gdx.app.debug("SamuraiArcherPlayer", "Attack/Cast Finished: " + currentAttackState);
                isAttacking = false;
                attackTimer = attackCooldown;
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
                // --- Minor Change: Clear hit enemy set when attack sequence finishes ---
                hitEnemiesThisAttack.clear();
                // --- End Minor Change ---
                projectileSpawnedThisAttack = false; // Reset spawn flag safely here too
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    private void spawnArrowProjectile() {
        if (projectileManager == null || position == null || bounds == null || assetLoader == null) return;
        Gdx.app.log("SamuraiArcherPlayer", "Spawning Arrow Projectile");
        float spawnOffsetY = bounds.height * 0.2f; // Adjust based on sprite
        float projectileWidth = 0;
        try {
            Texture projectileTexture = assetLoader.get(AssetLoader.SAMURAI_ARCHER_ARROW_PATH, Texture.class);
            if(ARROW_PROJ_COLS > 0) projectileWidth = (projectileTexture.getWidth() / ARROW_PROJ_COLS * 2.0f); // Projectile scale = 2.0f
        } catch (Exception e) { Gdx.app.error("SamuraiArcher", "Failed get arrow texture for width calc", e); }

        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f - projectileWidth;
        float spawnY = position.y + spawnOffsetY;
        float speed = GameConfig.PROJECTILE_SPEED * 1.5f;

        Projectile proj = new Projectile(assetLoader, spawnX, spawnY,
            facingRight ? speed : -speed, 0,
            ARROW_PROJ_DAMAGE, this,
            AssetLoader.SAMURAI_ARCHER_ARROW_PATH, // Projectile uses Arrow.png
            ARROW_PROJ_COLS, ARROW_PROJ_ROWS, ARROW_PROJ_FD);
        projectileManager.addProjectile(proj);
    }
}
