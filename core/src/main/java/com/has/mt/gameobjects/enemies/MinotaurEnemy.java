package com.has.mt.gameobjects.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.ai.BasicChaseAI; // Can use basic or create a more aggressive one later
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.interfaces.GameExceptionMessages;

public class MinotaurEnemy extends Enemy {

    private static final int MINOTAUR_HEALTH = 150;
    private static final int MINOTAUR_DAMAGE = 25;
    private static final float MINOTAUR_DETECT_RANGE = 380f;
    private static final float MINOTAUR_ATTACK_RANGE = 110f;
    private static final float MINOTAUR_COOLDOWN = 2.5f;
    private static final float MINOTAUR_SCALE_MOD = 1.3f;
    private static final float MINOTAUR_WALK_SPEED = 100f;


    public MinotaurEnemy(AssetLoader assetLoader, float x, float y) {
        super(assetLoader, x, y, GameConfig.ENEMY_SCALE * MINOTAUR_SCALE_MOD);

        this.healthComponent.setMaxHealth(MINOTAUR_HEALTH);
        this.healthComponent.reset();
        this.attackDamage = MINOTAUR_DAMAGE;
        this.detectRange = MINOTAUR_DETECT_RANGE;
        this.attackRange = MINOTAUR_ATTACK_RANGE;
        this.attackCooldownDuration = MINOTAUR_COOLDOWN;
        this.ai = new BasicChaseAI(this);
        ((BasicChaseAI) this.ai).moveSpeed = MINOTAUR_WALK_SPEED * 0.8f;
        ((BasicChaseAI) this.ai).runSpeed = MINOTAUR_WALK_SPEED;


        if (this.animationComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in MinotaurEnemy");
        }
        if (this.stateComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "StateComponent in MinotaurEnemy");
        }

        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "MinotaurEnemy", e);
        }
        Gdx.app.log("MinotaurEnemy", "Minotaur created at (" + x + ", " + y + ")");
    }

    @Override
    protected void setupAnimations() {
        // Frame counts VERIFIED from asset list
        animationComponent.addAnimation(State.IDLE, AssetLoader.MINOTAUR_IDLE_PATH, 10, 1, 0.20f, PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.MINOTAUR_WALK_PATH, 12, 1, 0.15f, PlayMode.LOOP);
        // Minotaur uses ATTACK1 state for its single Attack.png
        animationComponent.addAnimation(State.ATTACK1, AssetLoader.MINOTAUR_ATTACK_PATH, 5, 1, 0.12f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.MINOTAUR_HURT_PATH, 3, 1, 0.15f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.MINOTAUR_DEAD_PATH, 5, 1, 0.18f, PlayMode.NORMAL);

        // Link other states
        if (!animationComponent.hasAnimationForState(State.RUN)) {
            animationComponent.linkStateAnimation(State.RUN, State.WALK);
            Gdx.app.debug("MinotaurEnemy", "Linked RUN state to WALK animation");
        }
        if (!animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.JUMP, State.IDLE);
        }
        if (!animationComponent.hasAnimationForState(State.FALL)) {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }

        Gdx.app.log("MinotaurEnemy", "Animations setup complete.");
    }
}
