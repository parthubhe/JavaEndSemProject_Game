// src/com/has/mt/gameobjects/enemies/SlimeEnemy.java
package com.has.mt.gameobjects.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode; // Import PlayMode
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.ai.BasicChaseAI;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.gameobjects.Character; // Import Character for State enum

public class SlimeEnemy extends Enemy {

    private String slimeColor;

    public SlimeEnemy(AssetLoader assetLoader, float x, float y, String color) {
        super(assetLoader, x, y, GameConfig.ENEMY_SCALE * 0.8f);
        this.slimeColor = color;

        // Configure Slime properties
        this.healthComponent.setMaxHealth(50);
        this.healthComponent.reset();
        this.attackDamage = 8;
        this.detectRange = 350f;
        this.attackRange = 60f;
        this.attackCooldownDuration = 1.8f;
        this.ai = new BasicChaseAI(this); // Assign AI

        // Ensure components are initialized
        if (this.animationComponent == null) {
            throw new IllegalStateException("SlimeEnemy: AnimationComponent is null after super constructor!");
        }

        setupAnimations();
        Gdx.app.log("SlimeEnemy", slimeColor + " Slime created.");
    }

    @Override
    protected void setupAnimations() {
        String basePath = "Enemy/SlimeEnemy/" + slimeColor + "_Slime/";
        // Define paths, ensuring case sensitivity matches files
        String idlePath = basePath + "Idle.png";
        // Correct walk path based on previous errors/asset tree
        String walkPath = slimeColor.equals("Blue") ? basePath + "walk.png" : basePath + "Walk.png";
        String runPath = basePath + "Run.png";
        String jumpPath = basePath + "Jump.png";
        String attack1Path = basePath + "Attack_1.png";
        String attack2Path = basePath + "Attack_2.png"; // Add if exists
        String attack3Path = basePath + "Attack_3.png"; // Add if exists
        String hurtPath = basePath + "Hurt.png";
        String deadPath = basePath + "Dead.png";

        // Add animations using the component
        animationComponent.addAnimation(Character.State.IDLE, idlePath, 8, 1, 0.15f, PlayMode.LOOP);
        animationComponent.addAnimation(Character.State.WALK, walkPath, 8, 1, 0.12f, PlayMode.LOOP); // Assumes 8 frames
        animationComponent.addAnimation(Character.State.RUN, runPath, 7, 1, 0.1f, PlayMode.LOOP); // Assumes 7 frames
        animationComponent.addAnimation(Character.State.ATTACK1, attack1Path, 4, 1, 0.12f, PlayMode.NORMAL); // Assumes 4 frames
        // Add ATTACK2, ATTACK3 if they exist and are loaded in AssetLoader
        // animationComponent.addAnimation(Character.State.ATTACK2, attack2Path, 4, 1, 0.12f, PlayMode.NORMAL);
        // animationComponent.addAnimation(Character.State.ATTACK3, attack3Path, 5, 1, 0.12f, PlayMode.NORMAL); // Check frame counts
        animationComponent.addAnimation(Character.State.HURT, hurtPath, 6, 1, 0.1f, PlayMode.NORMAL); // Assumes 6 frames
        animationComponent.addAnimation(Character.State.DEAD, deadPath, 3, 1, 0.15f, PlayMode.NORMAL); // Assumes 3 frames

        // Add JUMP animation if the asset exists and is loaded
        animationComponent.addAnimation(Character.State.JUMP, jumpPath, 13, 1, 0.1f, PlayMode.NORMAL); // Assumes 13 frames for Jump


        // --- FIX: Use correct method names ---
        // Link FALL state if JUMP exists, otherwise link to IDLE
        if (!animationComponent.hasAnimationForState(Character.State.FALL)) { // Use hasAnimationForState
            if (animationComponent.hasAnimationForState(Character.State.JUMP)) {
                animationComponent.linkStateAnimation(Character.State.FALL, Character.State.JUMP); // Use linkStateAnimation
                // Optionally set play mode to reversed for fall
                // Animation<TextureRegion> fallAnim = animationComponent.getAnimation(Character.State.FALL); // Requires getAnimation method
                // if (fallAnim != null) fallAnim.setPlayMode(PlayMode.REVERSED);
            } else {
                animationComponent.linkStateAnimation(Character.State.FALL, Character.State.IDLE); // Fallback link
                Gdx.app.log("SlimeEnemy", "Linked state FALL to use animation from IDLE (fallback)");
            }
        }

        // Ensure JUMP state has an animation (fallback to IDLE if Jump.png wasn't loaded/doesn't exist)
        if (!animationComponent.hasAnimationForState(Character.State.JUMP)) {
            animationComponent.linkStateAnimation(Character.State.JUMP, Character.State.IDLE); // Use linkStateAnimation
            Gdx.app.log("SlimeEnemy", "Linked state JUMP to use animation from IDLE (fallback)");
        }
        // ------------------------------------

        Gdx.app.log("SlimeEnemy", "Animations setup complete for " + slimeColor + " Slime.");
    }

    // Override chooseAttackState if slime uses multiple attacks
    // @Override
    // protected Character.State chooseAttackState() {
    //     // Example: Randomly choose between available attacks
    //     int choice = MathUtils.random(0, 2); // Assuming ATTACK1, ATTACK2, ATTACK3 exist
    //     if (choice == 0) return Character.State.ATTACK1;
    //     if (choice == 1 && animationComponent.hasAnimationForState(Character.State.ATTACK2)) return Character.State.ATTACK2;
    //     if (choice == 2 && animationComponent.hasAnimationForState(Character.State.ATTACK3)) return Character.State.ATTACK3;
    //     return Character.State.ATTACK1; // Default if others missing
    // }

} // End of class
