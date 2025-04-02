package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.gameobjects.Player;
import com.has.mt.interfaces.GameExceptionMessages;

public class KnightPlayer extends Player {

    // Knight is purely melee in this version
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
        // Frame counts VERIFIED from previous attempt
        animationComponent.addAnimation(State.IDLE, AssetLoader.KNIGHT_IDLE_PATH, 4, 1, 0.15f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.KNIGHT_WALK_PATH, 8, 1, 0.1f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.KNIGHT_RUN_PATH, 7, 1, 0.08f, Animation.PlayMode.LOOP);
        animationComponent.addAnimation(State.JUMP, AssetLoader.KNIGHT_JUMP_PATH, 6, 1, 0.15f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.LIGHT_ATTACK, AssetLoader.KNIGHT_ATTACK1_PATH, 5, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HEAVY_ATTACK, AssetLoader.KNIGHT_ATTACK2_PATH, 4, 1, 0.1f, Animation.PlayMode.NORMAL);
        // Map Special1 input (E key) to Attack 3 for Knight
        animationComponent.addAnimation(State.ATTACK3, AssetLoader.KNIGHT_ATTACK3_PATH, 4, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.KNIGHT_HURT_PATH, 2, 1, 0.1f, Animation.PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.KNIGHT_DEAD_PATH, 6, 1, 0.15f, Animation.PlayMode.NORMAL);
        // Add Defend animation
        animationComponent.addAnimation(State.DEFEND, AssetLoader.KNIGHT_DEFEND_PATH, 5, 1, 0.1f, Animation.PlayMode.LOOP); // Loop defend anim

        // Link Fall state to Jump
        if (animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.FALL, State.JUMP);
        } else {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }
        Gdx.app.log("KnightPlayer", "Knight_1 animations setup complete.");
    }

    // Map Special1 (E key) to the Knight's ATTACK3 state
    @Override
    protected State mapSpecial1ToAction() {
        return State.ATTACK3;
    }

    // Knight doesn't use Special2 (V key) by default
    @Override
    protected State mapSpecial2ToAction() {
        return State.IDLE; // Or perhaps another ability if desired
    }

    @Override
    public void updateAttack(float delta) {
        if (isAttacking) {
            State currentAttackState = stateComponent.getCurrentState();
            boolean attackSequenceComplete = false;
            // boolean damageApplied = false; // Reset per attack sequence if needed

            // Check if animation for the current attack state is finished
            if (animationComponent.isAnimationFinished(currentAttackState)) {
                attackSequenceComplete = true;
                Gdx.app.debug("KnightPlayer", "Attack Animation Finished: " + currentAttackState);
            } else {
                // Simple damage timing (apply damage roughly halfway through)
                // CollisionManager actually checks overlap, this is just for conceptual timing
                float attackProgress = animationComponent.getStateTimer(currentAttackState);
                float attackDuration = animationComponent.getAnimationDuration(currentAttackState);
                float damagePoint = attackDuration * 0.5f;

                // Example: Trigger a damage check event at damagePoint (CollisionManager listens)
                // if (attackProgress >= damagePoint && !damageAppliedThisSequence) {
                //    eventManager.post(new DamageCheckEvent(this, calculateAttackHitbox(), getDamageForState(currentAttackState)));
                //    damageAppliedThisSequence = true;
                // }
            }

            // If attack animation/sequence is complete, reset state
            if (attackSequenceComplete) {
                isAttacking = false;
                attackTimer = attackCooldown; // Start cooldown
                State nextState = (physicsComponent != null && physicsComponent.isOnGround()) ? State.IDLE : State.FALL;
                stateComponent.setState(nextState);
            }
        } else if (attackTimer > 0) {
            // Decrease cooldown timer
            attackTimer -= delta;
        }
    }
}
