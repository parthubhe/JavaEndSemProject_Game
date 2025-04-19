// src/com/has/mt/gameobjects/players/LightningMagePlayer.java
package com.has.mt.gameobjects.players;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Projectile;
import com.has.mt.managers.ProjectileManager;


public class LightningMagePlayer extends Player {

    private ProjectileManager projectileManager;

    public LightningMagePlayer(AssetLoader assetLoader, float x, float y, ProjectileManager projectileManager) {
        super(assetLoader, x, y);
        this.projectileManager = projectileManager;
        if (this.animationComponent == null) { throw new IllegalStateException("AnimComp null"); }
        setupAnimations();
        Gdx.app.log("LightningMagePlayer", "Created.");
    }


    @Override
    protected void setupAnimations() {
        // Load primary animations
        animationComponent.addAnimation(Character.State.IDLE, AssetLoader.MAGE_IDLE_PATH, 7, 1, 0.15f, PlayMode.LOOP);
        animationComponent.addAnimation(Character.State.WALK, AssetLoader.MAGE_WALK_PATH, 7, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(Character.State.RUN, AssetLoader.MAGE_RUN_PATH, 8, 1, 0.1f, PlayMode.LOOP);
        animationComponent.addAnimation(Character.State.JUMP, AssetLoader.MAGE_JUMP_PATH, 8, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(Character.State.LIGHT_ATTACK, AssetLoader.MAGE_LIGHT_ATTACK_PATH, 10, 1, 0.08f, PlayMode.NORMAL);
        animationComponent.addAnimation(Character.State.HEAVY_ATTACK, AssetLoader.MAGE_HEAVY_ATTACK_PATH, 4, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(Character.State.CHARGED, AssetLoader.MAGE_CHARGED_PATH, 7, 1, 0.1f, PlayMode.NORMAL); // Charging animation
        animationComponent.addAnimation(Character.State.VADERSTRIKE, AssetLoader.MAGE_VADER_STRIKE_PATH, 13, 1, 0.09f, PlayMode.NORMAL);
        animationComponent.addAnimation(Character.State.HURT, AssetLoader.MAGE_HURT_PATH, 3, 1, 0.1f, PlayMode.NORMAL);
        animationComponent.addAnimation(Character.State.DEAD, AssetLoader.MAGE_DEAD_PATH, 5, 1, 0.15f, PlayMode.NORMAL);

        // --- FIX: Change LIGHTNING_BALL_CAST animation ---
        // Use a different animation for the player's casting action.
        // Option 1: Reuse a few frames of Light Attack (e.g., first 3 frames) - Requires AnimationLoader modification or manual frame extraction
        // Option 2: Use Heavy Attack animation if it looks like a cast preparation
        // Option 3: Link it to IDLE so the player just stands still briefly while casting
        // Let's link to IDLE for simplicity for now.
        if (animationComponent.hasAnimationForState(Character.State.IDLE)) {
            animationComponent.linkStateAnimation(Character.State.LIGHTNING_BALL_CAST, Character.State.IDLE);
            Gdx.app.log("LightningMagePlayer", "Linked LIGHTNING_BALL_CAST state to IDLE animation.");
            // If linking to IDLE, the updateAttack logic needs adjusting,
            // as isAnimationFinished(IDLE) will likely never be true if IDLE loops.
            // We need a timer or different state transition.
        } else {
            // Fallback if IDLE isn't even loaded (shouldn't happen)
            Gdx.app.error("LightningMagePlayer", "Cannot link LIGHTNING_BALL_CAST: IDLE animation missing.");
            // Set a dummy animation or handle error
        }
        // --- End Fix ---

        // Link FALL state logic (remains the same)
        if (!animationComponent.hasAnimationForState(Character.State.FALL) &&
            animationComponent.hasAnimationForState(Character.State.JUMP)) {
            animationComponent.linkStateAnimation(Character.State.FALL, Character.State.JUMP);
            // Optionally set play mode reversed etc.
        } else if (!animationComponent.hasAnimationForState(Character.State.FALL)) {
            animationComponent.linkStateAnimation(Character.State.FALL, Character.State.IDLE);
        }
        // Ensure JUMP has a fallback
        if (!animationComponent.hasAnimationForState(Character.State.JUMP)) {
            animationComponent.linkStateAnimation(Character.State.JUMP, Character.State.IDLE);
        }

        Gdx.app.log("LightningMagePlayer", "Animations set up.");
    }

    @Override
    protected void updateAttack(float delta) {
        if (isAttacking) {
            Character.State currentAttackState = stateComponent.getCurrentState();

            // --- FIX: Adjust logic for LIGHTNING_BALL_CAST if linked to IDLE ---
            // If the casting animation is now just IDLE (or very short),
            // we need to finish the attack sequence quickly after spawning the ball.
            boolean attackSequenceComplete = false;

            if (currentAttackState == Character.State.LIGHTNING_BALL_CAST) {
                // Since the animation might be IDLE (looping), don't wait for isAnimationFinished.
                // Consider the attack done almost immediately after spawning.
                // You might add a very small delay timer if needed.
                attackSequenceComplete = true; // Mark as complete
                Gdx.app.log("LightningMagePlayer", "Lightning Ball Cast state finishing.");

            } else { // Handle other attacks as before
                float attackProgress = animationComponent.getStateTimer(currentAttackState);
                float attackDuration = animationComponent.getAnimationDuration(currentAttackState);
                boolean damageAppliedThisAttack = false; // TODO: Improve this flag
                float damagePoint = attackDuration * 0.5f;

                // Damage Timing
                if (currentAttackState == Character.State.LIGHT_ATTACK && attackProgress >= damagePoint && !damageAppliedThisAttack) { triggerMeleeDamage(GameConfig.LIGHT_ATTACK_DAMAGE); damageAppliedThisAttack = true; }
                else if (currentAttackState == Character.State.HEAVY_ATTACK && attackProgress >= damagePoint && !damageAppliedThisAttack) { triggerMeleeDamage(GameConfig.HEAVY_ATTACK_DAMAGE); damageAppliedThisAttack = true; }
                else if (currentAttackState == Character.State.VADERSTRIKE && attackProgress >= damagePoint && !damageAppliedThisAttack) { triggerMeleeDamage(GameConfig.HEAVY_ATTACK_DAMAGE * 2); damageAppliedThisAttack = true; }

                // Projectile Spawning Transition
                if (currentAttackState == Character.State.CHARGED && animationComponent.isAnimationFinished(currentAttackState)) {
                    Gdx.app.log("LightningMagePlayer", "Charged finished, transitioning to cast state.");
                    stateComponent.setState(Character.State.LIGHTNING_BALL_CAST); // Now goes to IDLE (linked state)
                    animationComponent.resetStateTimer(Character.State.LIGHTNING_BALL_CAST);
                    spawnLightningBall();
                    // Do NOT set attackSequenceComplete here, wait for the CAST state next frame
                }
                // Normal Animation Finish Check (excluding the now immediate LIGHTNING_BALL_CAST)
                else if (animationComponent.isAnimationFinished(currentAttackState)) {
                    attackSequenceComplete = true;
                    Gdx.app.log("Player", "Attack Animation Finished: " + currentAttackState);
                }
            }

            // If any attack sequence finished this frame
            if (attackSequenceComplete) {
                isAttacking = false;
                attackTimer = attackCooldown;
                stateComponent.setState(physicsComponent.isOnGround() ? Character.State.IDLE : Character.State.FALL);
            }
            // --- End Fix ---

        } else if (attackTimer > 0) {
            attackTimer -= delta;
        }
    }

    // spawnLightningBall() remains the same, using MAGE_LIGHTNING_BALL_PATH for the projectile object's animation
    private void spawnLightningBall() {
        if (projectileManager == null) { Gdx.app.error("LMP", "ProjectileManager null!"); return; }
        Gdx.app.log("LMP", "Spawning Lightning Ball");
        float spawnX = facingRight ? position.x + bounds.width * 0.8f : position.x + bounds.width * 0.2f;
        float spawnY = position.y + bounds.height * 0.6f;
        String projAnimPath = AssetLoader.MAGE_LIGHTNING_BALL_PATH; // Projectile uses LM_Charge.png
        int pc = 9, pr = 1; float pfd = 0.1f;
        Projectile proj = new Projectile( assetLoader, spawnX, spawnY, facingRight ? GameConfig.PROJECTILE_SPEED : -GameConfig.PROJECTILE_SPEED, 0,
            GameConfig.PROJECTILE_DAMAGE, this, projAnimPath, pc, pr, pfd );
        projectileManager.addProjectile(proj);
    }
    private void triggerMeleeDamage(int d) { Gdx.app.log("LMP", "Trigger melee check: "+d); }

}
