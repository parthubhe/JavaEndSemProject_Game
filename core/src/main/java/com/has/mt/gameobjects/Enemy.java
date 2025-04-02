package com.has.mt.gameobjects;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException; // Import GameLogicException if needed for error handling
import com.has.mt.ai.EnemyAI;
import com.has.mt.interfaces.GameExceptionMessages; // Import messages

public abstract class Enemy extends Character {

    protected EnemyAI ai;
    protected float detectRange = GameConfig.ENEMY_DETECT_RANGE;
    protected float attackRange = GameConfig.ENEMY_ATTACK_RANGE;
    protected int attackDamage = GameConfig.ENEMY_BASE_DAMAGE;
    protected float attackCooldownTimer = 0f;
    protected float attackCooldownDuration = GameConfig.ENEMY_ATTACK_COOLDOWN;
    protected boolean canAttack = true;
    protected Character target; // Usually the player

    private boolean killProcessed = false;
    // --- CHANGE START: Flag to track damage dealt per attack instance ---
    protected boolean damageDealtThisAttack = false;
    // --- CHANGE END ---


    public Enemy(AssetLoader assetLoader, float x, float y, float scale) {
        super(assetLoader, x, y, scale);
    }

    @Override
    public void update(float delta) {
        // Safety check components
        if (stateComponent == null || physicsComponent == null || animationComponent == null || healthComponent == null || velocity == null) {
            Gdx.app.error("Enemy", "Critical component is null in update for " + this.getClass().getSimpleName());
            return;
        }

        if (!isAlive()) {
            if(stateComponent.isState(State.DEAD)) {
                animationComponent.update(State.DEAD, delta);
            }
            return;
        }

        // Update cooldown
        if (!canAttack) {
            attackCooldownTimer -= delta;
            if (attackCooldownTimer <= 0) {
                canAttack = true;
                attackCooldownTimer = 0;
            }
        }

        // Let the AI determine actions
        if (ai != null && target != null && target.isAlive()) {
            ai.update(delta);
        } else {
            velocity.x = 0;
            if(physicsComponent.isOnGround() && !stateComponent.isState(State.HURT) && !isAttacking()) {
                stateComponent.setState(State.IDLE);
            }
        }

        physicsComponent.update(delta);
        animationComponent.update(stateComponent.getCurrentState(), delta);

        // Handle state transitions (Hurt finish)
        if (stateComponent.isState(State.HURT) && isAnimationFinished(State.HURT)) {
            stateComponent.setState(physicsComponent.isOnGround() ? State.IDLE : State.FALL);
        }

        // Handle finishing attack states
        // --- CHANGE START: Reset damage flag when attack finishes ---
        if (isAttacking() && isAnimationFinished(stateComponent.getCurrentState())) {
            Gdx.app.debug("Enemy", this.getClass().getSimpleName() + " finished attack state: " + stateComponent.getCurrentState());
            stateComponent.setState(physicsComponent.isOnGround() ? State.IDLE : State.FALL);
            // Although damageDealtThisAttack should be reset when a *new* attack starts,
            // resetting it here ensures it's false when transitioning back to idle/fall.
            damageDealtThisAttack = false;
        }
        // --- CHANGE END ---
    }


    public void attemptAttack() {
        if (canAttack && isAlive() && target != null && target.isAlive()) {
            if (!isAttacking() && !stateComponent.isState(State.HURT))
            {
                State attackState = chooseAttackState();
                if (animationComponent.hasAnimationForState(attackState)) {
                    Gdx.app.log("Enemy", this.getClass().getSimpleName() + " starting attack: " + attackState);
                    stateComponent.setState(attackState);
                    animationComponent.resetStateTimer(attackState);
                    velocity.x = 0;
                    canAttack = false;
                    attackCooldownTimer = attackCooldownDuration;
                    // --- CHANGE START: Reset damage flag on new attack attempt ---
                    damageDealtThisAttack = false;
                    // --- CHANGE END ---
                } else {
                    Gdx.app.error("Enemy", this.getClass().getSimpleName() + " tried attack " + attackState + " but animation is missing!");
                    if(animationComponent.hasAnimationForState(State.ATTACK1)) {
                        stateComponent.setState(State.ATTACK1);
                        animationComponent.resetStateTimer(State.ATTACK1);
                        velocity.x = 0;
                        canAttack = false;
                        attackCooldownTimer = attackCooldownDuration;
                        damageDealtThisAttack = false; // Reset here too for fallback
                    } else {
                        stateComponent.setState(State.IDLE);
                    }
                }
            }
        }
    }

    @Deprecated
    protected void scheduleDamageApplication(float delay) { }


    protected State chooseAttackState() {
        return State.ATTACK1; // Default implementation
    }

    public void setTarget(Character target) {
        this.target = target;
        if (ai != null) {
            ai.setTarget(target);
        }
    }
    public EnemyAI getAI() { return ai; }
    public Character getTarget() { return target; }
    public float getDetectRange() { return detectRange; }
    public float getAttackRange() { return attackRange; }
    public int getAttackDamage() { return attackDamage; }

    // --- CHANGE START: Make isAttacking() check more specific if necessary ---
    // This check seems okay as it covers the common attack states.
    // If you add more specific attack states like WINDUP, ATTACK_EXECUTE, RECOVERY,
    // you'd adjust this to only return true during ATTACK_EXECUTE.
    public boolean isAttacking() {
        if(stateComponent == null) return false;
        State s = stateComponent.getCurrentState();
        return s == State.ATTACK1 || s == State.ATTACK2 || s == State.ATTACK3 ||
            s == State.LIGHT_ATTACK || s == State.HEAVY_ATTACK;
    }
    // --- CHANGE END ---


    public void drawDebug(ShapeRenderer shapeRenderer) {
        super.drawDebug(shapeRenderer);
        if (!GameConfig.DEBUG_DRAW_BOXES || shapeRenderer == null) return;

        if (bounds != null) {
            float centerX = position.x + bounds.width / 2;
            float centerY = position.y + bounds.height / 2;
            shapeRenderer.setColor(0, 0, 1, 0.5f);
            shapeRenderer.circle(centerX, centerY, detectRange);
            shapeRenderer.setColor(1, 1, 0, 0.5f);
            shapeRenderer.circle(centerX, centerY, attackRange);
        }

        if (ai != null && GameConfig.DEBUG_DRAW_PATHS) {
            ai.drawDebug(shapeRenderer);
        }
    }

    public boolean isKillProcessed() { return killProcessed; }
    public void markKillProcessed() { this.killProcessed = true; }

    // --- CHANGE START: Add methods to check and mark damage dealt ---
    /**
     * Checks if damage has already been dealt during the current attack animation cycle.
     * @return true if damage was dealt, false otherwise.
     */
    public boolean hasDealtDamageThisAttack() {
        return damageDealtThisAttack;
    }

    /**
     * Marks that damage has been dealt for the current attack animation cycle.
     * Called by CollisionManager after applying damage to the player.
     */
    public void markDamageDealtThisAttack() {
        this.damageDealtThisAttack = true;
        Gdx.app.debug("Enemy", this.getClass().getSimpleName() + " marked damage dealt for current attack.");
    }
    // --- CHANGE END ---


    // Enemy doesn't need updateAttack like Player
    @Override
    public void updateAttack(float delta) { }
}
