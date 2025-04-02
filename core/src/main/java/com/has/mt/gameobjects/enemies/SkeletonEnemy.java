package com.has.mt.gameobjects.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils; // Import MathUtils
import com.badlogic.gdx.utils.Array; // Import Array
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.ai.BasicChaseAI;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.interfaces.GameExceptionMessages;


public class SkeletonEnemy extends Enemy {

    private static final int SKELETON_HEALTH = 75;
    private static final int SKELETON_DAMAGE = 12;
    private static final float SKELETON_DETECT_RANGE = 450f;
    private static final float SKELETON_ATTACK_RANGE = 90f;
    private static final float SKELETON_COOLDOWN = 1.6f;
    private static final float SKELETON_SCALE_MOD = 1.0f;

    private boolean hasAttack2 = false;
    private boolean hasAttack3 = false;


    public SkeletonEnemy(AssetLoader assetLoader, float x, float y) {
        super(assetLoader, x, y, GameConfig.ENEMY_SCALE * SKELETON_SCALE_MOD);

        this.healthComponent.setMaxHealth(SKELETON_HEALTH);
        this.healthComponent.reset();
        this.attackDamage = SKELETON_DAMAGE;
        this.detectRange = SKELETON_DETECT_RANGE;
        this.attackRange = SKELETON_ATTACK_RANGE;
        this.attackCooldownDuration = SKELETON_COOLDOWN;
        this.ai = new BasicChaseAI(this);

        if (this.animationComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AnimationComponent in SkeletonEnemy");
        }
        if (this.stateComponent == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "StateComponent in SkeletonEnemy");
        }

        try {
            setupAnimations();
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionMessages.ANIMATION_SETUP_FAILED, "SkeletonEnemy", e);
        }

        Gdx.app.log("SkeletonEnemy", "Skeleton Warrior created at (" + x + ", " + y + ")");
    }

    @Override
    protected void setupAnimations() {
        // Frame counts VERIFIED from asset list
        animationComponent.addAnimation(State.IDLE, AssetLoader.SKELETON_IDLE_PATH, 7, 1, 0.18f, PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, AssetLoader.SKELETON_WALK_PATH, 7, 1, 0.12f, PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, AssetLoader.SKELETON_RUN_PATH, 8, 1, 0.10f, PlayMode.LOOP);
        animationComponent.addAnimation(State.ATTACK1, AssetLoader.SKELETON_ATTACK1_PATH, 5, 1, 0.10f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, AssetLoader.SKELETON_HURT_PATH, 2, 1, 0.15f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, AssetLoader.SKELETON_DEAD_PATH, 4, 1, 0.15f, PlayMode.NORMAL);

        // Conditionally load other attacks
        if (Gdx.files.internal(AssetLoader.SKELETON_ATTACK2_PATH).exists()) {
            animationComponent.addAnimation(State.ATTACK2, AssetLoader.SKELETON_ATTACK2_PATH, 6, 1, 0.09f, PlayMode.NORMAL);
            hasAttack2 = true;
        }
        if (Gdx.files.internal(AssetLoader.SKELETON_ATTACK3_PATH).exists()) {
            animationComponent.addAnimation(State.ATTACK3, AssetLoader.SKELETON_ATTACK3_PATH, 4, 1, 0.11f, PlayMode.NORMAL);
            hasAttack3 = true;
        }
        if (Gdx.files.internal(AssetLoader.SKELETON_PROTECT_PATH).exists()) {
            animationComponent.addAnimation(State.DEFEND, AssetLoader.SKELETON_PROTECT_PATH, 1, 1, 0.15f, PlayMode.LOOP);
        }


        // Link Jump/Fall to Idle
        if (!animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.JUMP, State.IDLE);
        }
        if (!animationComponent.hasAnimationForState(State.FALL)) {
            animationComponent.linkStateAnimation(State.FALL, State.IDLE);
        }

        Gdx.app.log("SkeletonEnemy", "Animations setup complete.");
    }

    // Override chooseAttackState for randomness
    @Override
    protected State chooseAttackState() {
        Array<State> availableAttacks = new Array<>(); // Corrected: Use imported Array
        availableAttacks.add(State.ATTACK1);
        if (hasAttack2) availableAttacks.add(State.ATTACK2);
        if (hasAttack3) availableAttacks.add(State.ATTACK3);

        if (availableAttacks.size == 1) {
            return State.ATTACK1;
        } else {
            // Example weighting: Attack1 more common
            float rand = MathUtils.random();
            if (rand < 0.6f) return State.ATTACK1; // 60% chance ATTACK1
            if (hasAttack2 && hasAttack3) {
                if(rand < 0.85f) return State.ATTACK2; // 25% chance ATTACK2
                else return State.ATTACK3; // 15% chance ATTACK3
            } else if (hasAttack2) {
                return State.ATTACK2; // Remaining 40% chance if only Attack2 exists
            } else if (hasAttack3) {
                return State.ATTACK3; // Remaining 40% chance if only Attack3 exists
            } else {
                return State.ATTACK1; // Fallback
            }
        }
    }
}
