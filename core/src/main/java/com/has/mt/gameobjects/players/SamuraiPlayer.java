package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.gameobjects.Player;
import com.has.mt.interfaces.GameExceptionMessages;

// Purely melee Samurai
public class SamuraiPlayer extends Player {

    public SamuraiPlayer(AssetLoader assetLoader, float x, float y) {
        super(assetLoader, x, y);

        if (this.animationComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in SamuraiPlayer");
        }
        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "SamuraiPlayer", e);
        }
        Gdx.app.log("SamuraiPlayer", "Samurai Player Created.");
    }

    @Override
    protected void setupAnimations() {
        Gdx.app.log("SamuraiPlayer", "Setting up Samurai animations...");
        // Frame counts based on guesses - VERIFY!
        animationComponent.addAnimation(State.IDLE, AssetLoader.SAMURAI_IDLE_PATH, 6, 1, 0.15f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.SAMURAI_WALK_PATH, 9, 1, 0.1f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.SAMURAI_RUN_PATH, 8, 1, 0.08f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.SAMURAI_JUMP_PATH, 9, 1, 0.15f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.SAMURAI_HURT_PATH, 3, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.SAMURAI_DEAD_PATH, 6, 1, 0.15f, Animation.PlayMode.NORMAL);

        // Map Attacks
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.SAMURAI_ATTACK1_PATH, 4, 1, 0.09f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.SAMURAI_ATTACK2_PATH, 5, 1, 0.11f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.ATTACK3, AssetLoader.SAMURAI_ATTACK3_PATH, 4, 1, 0.1f, Animation.PlayMode.NORMAL); // Map to ATTACK3 state

        // Defend/Protection
        animationComponent.addAnimation(State.DEFEND, AssetLoader.SAMURAI_PROTECTION_PATH, 2, 1, 0.2f, Animation.PlayMode.LOOP); // Assuming single frame loop

        // Link Fall state
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("SamuraiPlayer", "Samurai animations setup complete.");
    }

    // Map Special1 (E key) to ATTACK3
    @Override
    protected State mapSpecial1ToAction() { return State.ATTACK3; }

    // No default Special2 for Samurai
    @Override
    protected State mapSpecial2ToAction() { return State.IDLE; }

    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;

            // Check if animation for the current attack state is finished
            if (animationComponent.isAnimationFinished(currentAttackState)) {
                attackSequenceComplete = true;
                Gdx.app.debug("SamuraiPlayer", "Attack Animation Finished: " + currentAttackState);
            } else {
                // Melee damage timing (conceptual)
                // float attackProgress = animationComponent.getStateTimer(currentAttackState); ...
            }

            // If attack animation/sequence is complete, reset state
            if (attackSequenceComplete) {
                isAttacking = false;
                attackTimer = attackCooldown; // Start cooldown
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
            }
        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }
}
