package com.has.mt.gameobjects.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils; // Import MathUtils
import com.badlogic.gdx.utils.Array; // --- CHANGE START: Import Array ---
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.ai.BasicChaseAI;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.gameobjects.Character;

public class SlimeEnemy extends Enemy {

    private String slimeColor;
    private boolean hasAttack2 = false;
    private boolean hasAttack3 = false;

    public SlimeEnemy(AssetLoader assetLoader, float x, float y, String color) {
        super(assetLoader, x, y, GameConfig.ENEMY_SCALE * 0.8f);
        this.slimeColor = color;

        this.healthComponent.setMaxHealth(50);
        this.healthComponent.reset();
        this.attackDamage = 8;
        this.detectRange = 350f;
        this.attackRange = 60f;
        this.attackCooldownDuration = 1.8f;
        this.ai = new BasicChaseAI(this);

        if (this.animationComponent == null) {
            throw new IllegalStateException("SlimeEnemy: AnimationComponent is null after super constructor!");
        }

        setupAnimations();
        Gdx.app.log("SlimeEnemy", slimeColor + " Slime created.");
    }

    @Override
    protected void setupAnimations() {
        String basePath = "Enemy/SlimeEnemy/" + slimeColor + "_Slime/";
        String idlePath = basePath + "Idle.png";
        String walkPath = slimeColor.equals("Blue") ? basePath + "walk.png" : basePath + "Walk.png";
        String runPath = basePath + "Run.png";
        String jumpPath = basePath + "Jump.png";
        String attack1Path = basePath + "Attack_1.png";
        String attack2Path = basePath + "Attack_2.png";
        String attack3Path = basePath + "Attack_3.png";
        String hurtPath = basePath + "Hurt.png";
        String deadPath = basePath + "Dead.png";

        // --- Frame counts VERIFIED from asset list ---
        animationComponent.addAnimation(State.IDLE, idlePath, 8, 1, 0.15f, PlayMode.LOOP);
        animationComponent.addAnimation(State.WALK, walkPath, 8, 1, 0.12f, PlayMode.LOOP);
        animationComponent.addAnimation(State.RUN, runPath, 7, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(State.ATTACK1, attack1Path, 4, 1, 0.12f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.HURT, hurtPath, 6, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.DEAD, deadPath, 3, 1, 0.15f, PlayMode.NORMAL);
        animationComponent.addAnimation(State.JUMP, jumpPath, 13, 1, 0.1f, PlayMode.NORMAL);

        // Conditionally load and track other attacks
        if (Gdx.files.internal(attack2Path).exists()) {
            animationComponent.addAnimation(State.ATTACK2, attack2Path, 4, 1, 0.12f, PlayMode.NORMAL); // 4 frames?
            hasAttack2 = true;
        } else {
            Gdx.app.log("SlimeEnemy", "Attack 2 animation not found for " + slimeColor + ": " + attack2Path);
        }
        if (Gdx.files.internal(attack3Path).exists()) {
            animationComponent.addAnimation(State.ATTACK3, attack3Path, 5, 1, 0.12f, PlayMode.NORMAL); // 5 frames?
            hasAttack3 = true;
        } else {
            Gdx.app.log("SlimeEnemy", "Attack 3 animation not found for " + slimeColor + ": " + attack3Path);
        }

        // Link Fall state
        if (!animationComponent.hasAnimationForState(State.FALL)) {
            if (animationComponent.hasAnimationForState(State.JUMP)) {
                animationComponent.linkStateAnimation(State.FALL, State.JUMP);
            } else {
                animationComponent.linkStateAnimation(State.FALL, State.IDLE);
            }
        }
        if (!animationComponent.hasAnimationForState(State.JUMP)) {
            animationComponent.linkStateAnimation(State.JUMP, State.IDLE);
        }

        Gdx.app.log("SlimeEnemy", "Animations setup complete for " + slimeColor + " Slime.");
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
            return availableAttacks.random(); // Return a random attack from the available ones
        }
    }

}
