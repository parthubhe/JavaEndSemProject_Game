package com.has.mt.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException; // Import
import com.has.mt.components.AnimationComponent;
import com.has.mt.components.HealthComponent;
import com.has.mt.components.PhysicsComponent;
import com.has.mt.components.StateComponent;
import com.has.mt.interfaces.GameExceptionMessages; // Import
import com.has.mt.utils.DebugUtils;

public abstract class Character {
    public Vector2 position;
    public Vector2 velocity;
    public Rectangle bounds;
    public boolean facingRight = true;
    public float scale = 1.0f;

    public StateComponent stateComponent;
    public HealthComponent healthComponent;
    public PhysicsComponent physicsComponent;
    public AnimationComponent animationComponent;
    protected AssetLoader assetLoader;

    // Added DEFEND state
    public enum State {
        IDLE, WALK, RUN, JUMP, FALL,
        HURT, DEAD,
        // Generic Attacks (can be mapped by player classes)
        ATTACK1, ATTACK2, ATTACK3,
        // Specific Player Attacks/Abilities (mapped from generic inputs)
        LIGHT_ATTACK, HEAVY_ATTACK, // Standard Melee
        DEFEND, // Block/Protect State
        // Mage Specific
        CHARGED, // Lightning Mage Charge Start
        LIGHTNING_BALL_CAST, // Lightning Mage Projectile Cast
        VADERSTRIKE, // Lightning Mage Special
        FIREBALL_CAST, // Fire Wizard Projectile
        FLAME_JET_CAST, // Fire Wizard Special
        MAGIC_ARROW_CAST, // Wanderer Mage Projectile 1
        MAGIC_SPHERE_CAST, // Wanderer Mage Projectile 2
        // Archer Specific
        ARROW_SHOT // Samurai Archer Projectile
        // Add more specific states as needed
    }


    public Character(AssetLoader assetLoader, float x, float y, float scale) {
        if (assetLoader == null) { throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AssetLoader in Character constructor"); }
        this.assetLoader = assetLoader;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.bounds = new Rectangle(x, y, 0, 0); // Initialize bounds, size set in render
        this.scale = scale > 0 ? scale : 1.0f; // Ensure positive scale

        // Initialize components
        this.stateComponent = new StateComponent(State.IDLE);
        this.healthComponent = new HealthComponent(100); // Default health, subclasses should override
        this.physicsComponent = new PhysicsComponent(this);
        this.animationComponent = new AnimationComponent(this.assetLoader);
    }

    public abstract void update(float delta);
    protected abstract void setupAnimations();

    public void render(SpriteBatch batch) {
        if (animationComponent == null || stateComponent == null || batch == null) return; // Safety check
        TextureRegion currentFrame = animationComponent.getCurrentFrame(stateComponent.getCurrentState());
        if (currentFrame == null) {
            // Gdx.app.error("Character", "No frame found for state: " + stateComponent.getCurrentState() + " for " + this.getClass().getSimpleName());
            currentFrame = animationComponent.getCurrentFrame(State.IDLE); // Try idle as fallback
            if(currentFrame == null) return; // Give up if idle is also missing
        }


        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;

        if (bounds != null) { // Safety check
            bounds.set(position.x, position.y, frameWidth, frameHeight);
        }

        if (facingRight) {
            batch.draw(currentFrame, position.x, position.y, frameWidth, frameHeight);
        } else {
            // Flip horizontally by drawing with negative width and offset X
            batch.draw(currentFrame, position.x + frameWidth, position.y, -frameWidth, frameHeight);
        }
    }


    public void drawDebug(ShapeRenderer shapeRenderer) {
        if (!GameConfig.DEBUG_DRAW_BOXES || shapeRenderer == null) return;
        if (bounds != null) { // Safety check
            DebugUtils.drawDebugLines(shapeRenderer, this);
        }
    }

    public void takeDamage(int amount) {
        if (healthComponent == null || stateComponent == null || animationComponent == null) return; // Safety

        // Damage reduction/negation if defending
        if (stateComponent.isState(State.DEFEND)) {
            // Example: Reduce damage by 80% when defending
            amount *= 0.2f; // Make damage only 20%
            if(amount < 1 && amount > 0) amount = 1; // Ensure at least 1 damage if not fully blocked
            Gdx.app.log("Character", "Damage reduced by Defend state. Taking: " + amount);
            // Add visual/sound effect for block here
        }

        if (healthComponent.isAlive() && amount > 0) { // Only apply positive damage
            healthComponent.decreaseHealth(amount);
            if (healthComponent.isAlive()) {
                // Don't interrupt an existing hurt animation, or death animation
                if (!stateComponent.isState(State.HURT) && !stateComponent.isState(State.DEAD)) {
                    stateComponent.setState(State.HURT);
                    animationComponent.resetStateTimer(State.HURT);
                    // Optional knockback can be added here
                }
            } else {
                die();
            }
        }
    }

    protected void die() {
        if (stateComponent == null || velocity == null || animationComponent == null) return;
        if (!stateComponent.isState(State.DEAD)) {
            Gdx.app.log("Character", this.getClass().getSimpleName() + " Died at " + position);
            stateComponent.setState(State.DEAD);
            velocity.set(0, 0); // Stop movement
            animationComponent.resetStateTimer(State.DEAD);
        }
    }

    public boolean isAnimationFinished(State state) {
        return animationComponent != null && animationComponent.isAnimationFinished(state);
    }

    public State getCurrentState() {
        return stateComponent != null ? stateComponent.getCurrentState() : State.IDLE;
    }

    public boolean isAlive() {
        return healthComponent != null && healthComponent.isAlive();
    }

    // --- CHANGE START: Make updateAttack abstract and public ---
    // Subclasses MUST implement how they handle attack timing, state changes, etc.
    public abstract void updateAttack(float delta);
    // --- CHANGE END ---


    public void dispose() {
        if (animationComponent != null) {
            animationComponent.dispose();
        }
    }
}
