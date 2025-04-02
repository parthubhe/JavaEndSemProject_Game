package com.has.mt.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException;
import com.has.mt.interfaces.GameExceptionMessages;
import com.has.mt.managers.InputManager;


public abstract class Player extends Character {
    protected float moveSpeed = GameConfig.PLAYER_MOVE_SPEED;
    protected float runSpeed = GameConfig.PLAYER_RUN_SPEED;
    protected float jumpVelocity = GameConfig.PLAYER_JUMP_VELOCITY;
    protected boolean wantsToJump = false;
    protected boolean isRunning = false;
    protected boolean isAttacking = false; // Flag indicating player initiated an attack sequence
    protected float attackTimer = 0f; // Cooldown timer
    protected float attackCooldown = GameConfig.ATTACK_COOLDOWN;

    private float invulnerableTimer = 0f;
    private static final float INVULNERABILITY_DURATION = 0.75f;

    protected int killCount = 0;
    protected boolean wantsToDefend = false; // Flag for Defend input


    public Player(AssetLoader assetLoader, float x, float y) {
        super(assetLoader, x, y, GameConfig.PLAYER_SCALE);
        if (this.healthComponent != null) {
            this.healthComponent.setMaxHealth(GameConfig.PLAYER_START_HEALTH);
            this.healthComponent.reset();
        } else {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "HealthComponent in Player constructor");
        }
    }

    @Override
    public void update(float delta) {
        if (invulnerableTimer > 0) {
            invulnerableTimer -= delta;
        }

        if (stateComponent == null || physicsComponent == null || animationComponent == null || velocity == null || healthComponent == null || position == null) {
            Gdx.app.error("Player", "A critical component is null in update!");
            return;
        }

        if (!isAlive()) {
            if (stateComponent.isState(State.DEAD)) {
                animationComponent.update(State.DEAD, delta);
            }
            return;
        }

        handleInput(); // Read input flags
        physicsComponent.update(delta); // Apply physics and check ground
        updateState(); // Determine state based on flags and physics
        animationComponent.update(stateComponent.getCurrentState(), delta); // Update animation timer
        updateAttack(delta); // Handle attack timing/logic/cooldown

        // Reset single-frame intent flags AFTER state update used them
        wantsToJump = false;
        isRunning = false;
        // wantsToDefend is reset inside handleInput based on key press state
    }

    protected void handleInput() {
        if (stateComponent == null || physicsComponent == null || velocity == null) return; // Safety

        // Prevent most input during HURT state
        if (stateComponent.isState(State.HURT)) {
            return;
        }

        // Check Defend input first (Q key)
        wantsToDefend = InputManager.isActionPressed(InputManager.Action.DEFEND);

        // Reset horizontal velocity logic
        if (!isAttacking && !stateComponent.isState(State.DEFEND)) {
            velocity.x = 0; // Allow movement to overwrite this
        } else {
            velocity.x = 0; // Force stop if attacking or defending
        }

        // Movement (only if not attacking or defending)
        if (!isAttacking && !stateComponent.isState(State.DEFEND)) {
            if (InputManager.isActionPressed(InputManager.Action.MOVE_LEFT)) {
                isRunning = InputManager.isActionPressed(InputManager.Action.RUN);
                velocity.x = -(isRunning ? runSpeed : moveSpeed);
                facingRight = false;
            } else if (InputManager.isActionPressed(InputManager.Action.MOVE_RIGHT)) {
                isRunning = InputManager.isActionPressed(InputManager.Action.RUN);
                velocity.x = (isRunning ? runSpeed : moveSpeed);
                facingRight = true;
            }
        }

        // Jump (only if on ground and not attacking/defending)
        if (physicsComponent.isOnGround() && !isAttacking && !stateComponent.isState(State.DEFEND)) {
            if (InputManager.isActionJustPressed(InputManager.Action.JUMP)) {
                wantsToJump = true;
            }
        }

        // Attacks (check cooldown, not already attacking, not defending, and in appropriate state)
        if (attackTimer <= 0 && !isAttacking && !stateComponent.isState(State.DEFEND)) {
            // Allow attacks from ground or maybe falling? For now, ground only.
            if(physicsComponent.isOnGround()) {
                if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_LIGHT)) {
                    startAttack(State.LIGHT_ATTACK);
                } else if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_HEAVY)) {
                    startAttack(State.HEAVY_ATTACK);
                } else if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_SPECIAL1)) {
                    startAttack(mapSpecial1ToAction());
                } else if (InputManager.isActionJustPressed(InputManager.Action.ATTACK_SPECIAL2)) {
                    startAttack(mapSpecial2ToAction());
                }
            }
        }
    }

    // Maps the generic "Special 1" input (e.g., E key) to a character-specific State
    // Subclasses MUST override this to return the correct State for their primary special ability.
    protected State mapSpecial1ToAction() {
        Gdx.app.log(this.getClass().getSimpleName(),"mapSpecial1ToAction not overridden, returning IDLE.");
        return State.IDLE;
    }

    // Maps the generic "Special 2" input (e.g., V key) to a character-specific State
    // Subclasses MUST override this if they have a secondary special ability.
    protected State mapSpecial2ToAction() {
        Gdx.app.log(this.getClass().getSimpleName(),"mapSpecial2ToAction not overridden, returning IDLE.");
        return State.IDLE;
    }

    protected void startAttack(State attackState) {
        if (attackState == State.IDLE || attackState == null) return; // Don't start an invalid "attack"

        if (!isAttacking && physicsComponent != null && stateComponent != null && animationComponent != null) {
            State currentState = stateComponent.getCurrentState();
            // Define states from which attacks can be initiated
            boolean canAttackFromCurrentState = (currentState == State.IDLE || currentState == State.WALK || currentState == State.RUN); // Add JUMP/FALL later if needed

            if (canAttackFromCurrentState) {
                // Check if this character *has* the requested attack animation
                if (!animationComponent.hasAnimationForState(attackState)) {
                    Gdx.app.log("Player", "Attack state " + attackState + " requested but no animation found for " + this.getClass().getSimpleName());
                    return; // Don't start attack if animation is missing
                }

                Gdx.app.log("Player", "Starting Attack: " + attackState);
                isAttacking = true; // Set the attacking flag
                stateComponent.setState(attackState); // Set the character's state
                animationComponent.resetStateTimer(attackState); // Start the animation from the beginning
                velocity.x = 0; // Stop horizontal movement during the attack (optional, can be adjusted per attack)
            }
        }
    }

    protected void updateState() {
        if (stateComponent == null || physicsComponent == null || animationComponent == null) return;

        // State Priority: Dead > Hurt > Attack > Defend > Jump/Fall > Run/Walk > Idle
        if (stateComponent.isState(State.DEAD)) return;

        if (stateComponent.isState(State.HURT)) {
            if (animationComponent.isAnimationFinished(State.HURT)) {
                stateComponent.setState(physicsComponent.isOnGround() ? State.IDLE : State.FALL);
            }
            return;
        }

        // If currently attacking, let updateAttack handle state changes upon completion
        if (isAttacking) {
            // updateAttack in the subclass should set isAttacking = false and change state when done.
            return;
        }

        // Handle Defend State
        // Enter Defend state if Q is pressed, on ground, and has Defend animation
        if (wantsToDefend && physicsComponent.isOnGround() && animationComponent.hasAnimationForState(State.DEFEND)) {
            if (!stateComponent.isState(State.DEFEND)) {
                Gdx.app.log("Player", "Entering Defend State");
                stateComponent.setState(State.DEFEND);
                animationComponent.resetStateTimer(State.DEFEND);
                velocity.x = 0; // Stop horizontal movement
            }
            return; // Stay in Defend state if Q is held
        }
        // Exit Defend state if Q is released (or no longer wantsToDefend) while in Defend state
        if (stateComponent.isState(State.DEFEND) && !wantsToDefend) {
            Gdx.app.log("Player", "Exiting Defend State");
            stateComponent.setState(State.IDLE); // Or FALL if somehow in air?
            return;
        }


        // Handle Jump/Fall states (if not defending)
        if (wantsToJump && physicsComponent.isOnGround()) {
            physicsComponent.jump(jumpVelocity);
            stateComponent.setState(State.JUMP);
            animationComponent.resetStateTimer(State.JUMP);
        } else if (!physicsComponent.isOnGround()) {
            // Use velocity to determine jump vs fall (can be refined, maybe needs buffer)
            stateComponent.setState(velocity.y >= 0 ? State.JUMP : State.FALL);
        } else { // On ground and not Jumping/Falling/Attacking/Defending/Hurt
            if (velocity.x != 0) {
                stateComponent.setState(isRunning ? State.RUN : State.WALK);
            } else {
                stateComponent.setState(State.IDLE);
            }
        }
    }

    // --- CHANGE START: Make updateAttack abstract and public ---
    // Now correctly overriding the abstract method from Character
    @Override
    public abstract void updateAttack(float delta);
    // --- CHANGE END ---

    // --- CHANGE START: Add public isAttacking method ---
    /**
     * Checks if the player is currently in an attack sequence.
     * @return true if the player is attacking, false otherwise.
     */
    public boolean isAttacking() {
        return isAttacking;
    }
    // --- CHANGE END ---

    @Override
    public void takeDamage(int amount) {
        // Incorporate DEFEND check from Character class if not overridden here.
        super.takeDamage(amount); // Call Character's takeDamage which handles DEFEND state.
    }


    public void reset(float x, float y) {
        if (position != null) position.set(x, y);
        if (velocity != null) velocity.set(0, 0);
        if (healthComponent != null) healthComponent.reset();
        if (stateComponent != null) stateComponent.setState(State.IDLE);
        if (physicsComponent != null) physicsComponent.reset();
        facingRight = true;
        isAttacking = false;
        attackTimer = 0f;
        invulnerableTimer = 0f;
        killCount = 0;
        wantsToDefend = false;
        Gdx.app.log("Player", this.getClass().getSimpleName() + " reset to (" + x + "," + y + ")");
    }

    public boolean isInvulnerable() { return invulnerableTimer > 0; }
    public void registerKill() { this.killCount++; }
    public int getTotalKills() { return this.killCount; }
}
