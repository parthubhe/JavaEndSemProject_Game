// src/com/has/mt/gameobjects/Player.java
package com.has.mt.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.managers.InputManager; // Assume an InputManager exists

public abstract class Player extends Character {
    protected float moveSpeed = GameConfig.PLAYER_MOVE_SPEED;
    protected float runSpeed = GameConfig.PLAYER_RUN_SPEED;
    protected float jumpVelocity = GameConfig.PLAYER_JUMP_VELOCITY;
    protected boolean wantsToJump = false;
    protected boolean isRunning = false;
    protected boolean isAttacking = false;
    protected float attackTimer = 0f;
    protected float attackCooldown = GameConfig.ATTACK_COOLDOWN;

    // --- FIX: Add invulnerability timer ---
    private float invulnerableTimer = 0f;
    private static final float INVULNERABILITY_DURATION = 0.75f;
    // --------------------------------------

    public Player(AssetLoader assetLoader, float x, float y) {
        super(assetLoader, x, y, GameConfig.PLAYER_SCALE);
        if (this.healthComponent != null) { // Null check
            this.healthComponent.setMaxHealth(GameConfig.PLAYER_START_HEALTH);
            this.healthComponent.reset();
        } else {
            Gdx.app.error("Player", "HealthComponent is null in constructor!");
        }
        // setupAnimations() MUST be called by the concrete subclass constructor
    }

    @Override
    public void update(float delta) {
        // Decrease invulnerability timer
        if (invulnerableTimer > 0) {
            invulnerableTimer -= delta;
        }

        if (!isAlive()) {
            // Update death animation
            if (animationComponent != null) animationComponent.update(stateComponent.getCurrentState(), delta);
            return; // No input/physics/state changes when dead
        }

        handleInput();
        if (physicsComponent!= null) physicsComponent.update(delta); // Check component null
        updateState(); // Update state AFTER physics
        if (animationComponent != null) animationComponent.update(stateComponent.getCurrentState(), delta);
        updateAttack(delta);

        // Reset flags for next frame
        wantsToJump = false;
        isRunning = false;
    }

    // --- FIX: Modify takeDamage to check invulnerability ---
    @Override
    public void takeDamage(int amount) {
        // Only take damage if not currently invulnerable and alive
        if (invulnerableTimer <= 0 && healthComponent.isAlive()) {
            Gdx.app.log("Player", "Taking damage: " + amount);
            healthComponent.decreaseHealth(amount);
            invulnerableTimer = INVULNERABILITY_DURATION; // Become invulnerable

            if (healthComponent.isAlive()) {
                stateComponent.setState(State.HURT);
                if (animationComponent!= null) animationComponent.resetStateTimer(State.HURT);
                // Optional: Add knockback effect
                // velocity.x = facingRight ? -150f : 150f; // Knock back slightly
                // velocity.y = 100f; // Pop up slightly
            } else {
                die(); // Call die method from Character base class
            }
        } else if (healthComponent.isAlive()) {
            Gdx.app.log("Player", "Damage ignored (Invulnerable). Timer: " + invulnerableTimer);
        }
    }
    // ----------------------------------------------------

    public boolean isAttacking() {
        return this.isAttacking;
    }

    protected void handleInput() {
        // Reset horizontal velocity each frame unless key is pressed
        if (velocity != null) velocity.x = 0; // Null check
        else return; // Cannot proceed without velocity

        // Check input only if not hurt (allow movement cancelling hurt?)
        if (stateComponent != null && stateComponent.isState(State.HURT)) {
            // Optionally allow horizontal control during hurt state?
            // For now, prevent input during hurt.
            return;
        }

        // Movement
        if (InputManager.isActionPressed(InputManager.Action.MOVE_LEFT)) {
            isRunning = InputManager.isActionPressed(InputManager.Action.RUN);
            velocity.x = -(isRunning ? runSpeed : moveSpeed);
            facingRight = false;
        } else if (InputManager.isActionPressed(InputManager.Action.MOVE_RIGHT)) {
            isRunning = InputManager.isActionPressed(InputManager.Action.RUN);
            velocity.x = (isRunning ? runSpeed : moveSpeed);
            facingRight = true;
        }

        // Jump (check physicsComponent exists)
        if (InputManager.isActionJustPressed(InputManager.Action.JUMP) && physicsComponent != null && physicsComponent.isOnGround()) {
            wantsToJump = true;
        }

        // Attacks
        if (attackTimer <= 0 && !isAttacking) {
            if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_LIGHT)) {
                startAttack(State.LIGHT_ATTACK);
            } else if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_HEAVY)) {
                startAttack(State.HEAVY_ATTACK);
            } else if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_SPECIAL1)) { // Charged
                startAttack(State.CHARGED);
            } else if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_SPECIAL2)) { // Vaderstrike
                startAttack(State.VADERSTRIKE);
            }
        }
    }

    // Simplified startAttack - duration is handled by AnimationComponent
    protected void startAttack(State attackState) {
        // Add null checks for components
        if (!isAttacking && physicsComponent != null && physicsComponent.isOnGround() &&
            stateComponent != null && animationComponent != null)
        {
            Gdx.app.log("Player", "Starting Attack: " + attackState);
            isAttacking = true;
            stateComponent.setState(attackState);
            animationComponent.resetStateTimer(attackState);
            velocity.x = 0; // Stop movement during attack (optional)
        }
    }


    protected void updateState() {
        // Ensure components exist
        if (stateComponent == null || physicsComponent == null || animationComponent == null) return;

        // Prioritize Hurt/Attack states
        if (isAttacking) { return; } // Let updateAttack handle finishing attack state
        if (stateComponent.isState(State.HURT)) {
            if (animationComponent.isAnimationFinished(State.HURT)) {
                // Finished being hurt, return to normal state
                stateComponent.setState(physicsComponent.isOnGround() ? State.IDLE : State.FALL);
            }
            return; // Don't change state further if hurt
        }

        // Handle movement states
        if (wantsToJump && physicsComponent.isOnGround()) {
            physicsComponent.jump(jumpVelocity);
            stateComponent.setState(State.JUMP);
            animationComponent.resetStateTimer(State.JUMP);
        } else if (!physicsComponent.isOnGround()) {
            // Use velocity to determine jump vs fall (can be refined)
            stateComponent.setState(velocity.y >= 0 ? State.JUMP : State.FALL);
        } else { // On ground
            if (velocity.x != 0) {
                stateComponent.setState(isRunning ? State.RUN : State.WALK);
            } else {
                stateComponent.setState(State.IDLE);
            }
        }
    }

    // Abstract updateAttack needs to be implemented by subclasses if defined as abstract
    // If not abstract, provide a base implementation or remove if handled entirely here/in specific player
    protected abstract void updateAttack(float delta); // Make sure this matches base Player definition


    public void reset(float x, float y) {
        // Add null checks
        if (position != null) position.set(x, y);
        if (velocity != null) velocity.set(0, 0);
        if (healthComponent != null) healthComponent.reset();
        if (stateComponent != null) stateComponent.setState(State.IDLE);
        facingRight = true;
        isAttacking = false;
        attackTimer = 0f;
        invulnerableTimer = 0f; // Reset invulnerability
        if (physicsComponent != null) physicsComponent.reset();
    }

    // Method to check invulnerability status (optional)
    public boolean isInvulnerable() {
        return invulnerableTimer > 0;
    }

}
