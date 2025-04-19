// src/com/has/mt/gameobjects/Enemy.java
package com.has.mt.gameobjects;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.ai.EnemyAI;
public abstract class Enemy extends Character {

    protected EnemyAI ai;
    protected float detectRange = GameConfig.ENEMY_DETECT_RANGE;
    protected float attackRange = GameConfig.ENEMY_ATTACK_RANGE;
    protected int attackDamage = GameConfig.ENEMY_BASE_DAMAGE;
    protected float attackCooldownTimer = 0f;
    protected float attackCooldownDuration = GameConfig.ENEMY_ATTACK_COOLDOWN;
    protected boolean canAttack = true;
    protected Character target; // Usually the player

    // NEW: Track if kill was processed
    private boolean killProcessed = false;


    public Enemy(AssetLoader assetLoader, float x, float y, float scale) {
        super(assetLoader, x, y, scale); // Use specific scale if needed
        // Subclasses should set health, AI, ranges, damage
        // setupAnimations() MUST be called by the concrete subclass constructor
    }

    @Override
    public void update(float delta) {
        if (!isAlive()) {
            // Update death animation
            animationComponent.update(stateComponent.getCurrentState(), delta);
            // If death animation finished, mark for removal?
            return;
        }

        // Update cooldown
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
            if (attackCooldownTimer <= 0) {
                canAttack = true;
            }
        }

        // Let the AI determine actions (movement, state changes)
        if (ai != null && target != null && target.isAlive()) {
            ai.update(delta); // AI will likely change velocity and state
        } else {
            // Default behavior if no AI or target (e.g., idle)
            velocity.x = 0;
            if(physicsComponent.isOnGround() && stateComponent.getCurrentState() != State.HURT) {
                stateComponent.setState(State.IDLE);
            }
        }

        // Update physics (applies velocity, gravity, ground check)
        physicsComponent.update(delta);

        // Update animation timer for the current state set by AI or physics
        // Ensure state is valid before updating animation
        if (stateComponent.getCurrentState() != null) {
            animationComponent.update(stateComponent.getCurrentState(), delta);
        }


        // Handle state transitions after AI/Physics (e.g., if hurt animation finishes)
        if (stateComponent.getCurrentState() == State.HURT && isAnimationFinished(State.HURT)) {
            // Transition back to a default state after being hurt
            stateComponent.setState(physicsComponent.isOnGround() ? State.IDLE : State.FALL);
            // AI might override this immediately in the next frame
        }
    }

    // Called by AI when an attack should occur
    public void attemptAttack() {
        if (canAttack && isAlive() && target != null && target.isAlive()) {
            if (stateComponent.getCurrentState() != State.ATTACK1 && // Check not already attacking
                stateComponent.getCurrentState() != State.ATTACK2 &&
                stateComponent.getCurrentState() != State.ATTACK3 &&
                stateComponent.getCurrentState() != State.HURT)
            {
                Gdx.app.log("Enemy", this.getClass().getSimpleName() + " starting attack");
                // Choose an attack type (can be random or specific)
                State attackState = chooseAttackState();
                stateComponent.setState(attackState);
                animationComponent.resetStateTimer(attackState);
                velocity.x = 0; // Often stop moving during attack
                canAttack = false; // Prevent immediate re-attack
                attackCooldownTimer = attackCooldownDuration;

                // Damage application needs timing: either via animation events,
                // or a delayed action based on attackState duration.
                // Example: Schedule damage application after half the animation duration
                scheduleDamageApplication(animationComponent.getAnimationDuration(attackState) * 0.5f); // Needs a timer mechanism
            }
        }
    }

    // Placeholder for damage timing - needs a proper event/timer system
    protected void scheduleDamageApplication(float delay) {
        // In a real system, use a Timer, DelayedAction, or check elapsed time in update
        Gdx.app.log("Enemy", "Damage scheduled in " + delay + "s");
        // For now, we'll approximate damage check in CollisionManager based on attack state
    }


    // Subclasses can override this to select different attacks
    protected State chooseAttackState() {
        return State.ATTACK1; // Default to first attack
    }

    public void setTarget(Character target) {
        this.target = target;
        if (ai != null) {
            ai.setTarget(target);
        }
    }
    public EnemyAI getAI() {
        return ai;
    }
    public Character getTarget() {
        return target;
    }

    public float getDetectRange() {
        return detectRange;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public boolean isAttacking() {
        State s = stateComponent.getCurrentState();
        return s == State.ATTACK1 || s == State.ATTACK2 || s == State.ATTACK3;
    }

    // Method for debug drawing AI info
    public void drawDebug(ShapeRenderer shapeRenderer) {
        if (GameConfig.DEBUG_DRAW_BOXES) {
            // Draw bounding box
            shapeRenderer.setColor(1, 0, 0, 1); // Red
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

            // Draw detection range
            shapeRenderer.setColor(0, 0, 1, 1); // Blue
            shapeRenderer.circle(position.x + bounds.width / 2, position.y + bounds.height / 2, detectRange);

            // Draw attack range
            shapeRenderer.setColor(1, 1, 0, 1); // Yellow
            shapeRenderer.circle(position.x + bounds.width / 2, position.y + bounds.height / 2, attackRange);
        }
        if (ai != null && GameConfig.DEBUG_DRAW_PATHS) {
            ai.drawDebug(shapeRenderer);
        }
    }

    public boolean isKillProcessed() {
        return killProcessed;
    }

    public void markKillProcessed() {
        this.killProcessed = true;
    }
}
