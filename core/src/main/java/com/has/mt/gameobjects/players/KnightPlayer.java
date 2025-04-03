package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.gameobjects.Player;
import com.has.mt.interfaces.GameExceptionMessages;

public class KnightPlayer extends Player {

    public KnightPlayer(AssetLoader assetLoader, float x, float y) {
        super(assetLoader, x, y);
        if (this.animationComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in KnightPlayer");
        }
        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "KnightPlayer", e);
        }
        Gdx.app.log("KnightPlayer", "Knight_1 Player Created.");
    }

    @Override
    protected void setupAnimations() {
        Gdx.app.log("KnightPlayer", "Setting up Knight_1 animations...");
        animationComponent.addAnimation(State.IDLE, AssetLoader.KNIGHT_IDLE_PATH, 4, 1, 0.15f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.KNIGHT_WALK_PATH, 8, 1, 0.1f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.KNIGHT_RUN_PATH, 7, 1, 0.08f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.KNIGHT_JUMP_PATH, 6, 1, 0.15f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.KNIGHT_ATTACK1_PATH, 5, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.KNIGHT_ATTACK2_PATH, 4, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.ATTACK3, AssetLoader.KNIGHT_ATTACK3_PATH, 4, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.KNIGHT_HURT_PATH, 2, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.KNIGHT_DEAD_PATH, 6, 1, 0.15f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEFEND, AssetLoader.KNIGHT_DEFEND_PATH, 5, 1, 0.1f, Animation.PlayMode.LOOP);

        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("KnightPlayer", "Knight_1 animations setup complete.");
    }

    @Override
    protected State mapSpecial1ToAction() { return State.ATTACK3; }
    @Override
    protected State mapSpecial2ToAction() { return State.IDLE; }

    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            if (animationComponent.isAnimationFinished(currentAttackState)) {
                attackSequenceComplete = true;
                Gdx.app.debug("KnightPlayer", "Attack Animation Finished: " + currentAttackState);
            } else {
                // Damage timing logic would go here if needed (e.g., applying damage at a specific frame)
                // But for simpler melee, we rely on CollisionManager checking overlap during the attack state.
            }

            if (attackSequenceComplete) {
                isAttacking = false;
                attackTimer = attackCooldown; // Start cooldown
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
}
