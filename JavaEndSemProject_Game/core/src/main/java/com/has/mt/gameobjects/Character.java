// src/com/has/mt/gameobjects/Character.java
package com.has.mt.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer; // Import ShapeRenderer
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig; // Import GameConfig
import com.has.mt.components.AnimationComponent;
import com.has.mt.components.HealthComponent;
import com.has.mt.components.PhysicsComponent;
import com.has.mt.components.StateComponent;
import com.has.mt.utils.DebugUtils; // Import DebugUtils

public abstract class Character {
    public Vector2 position;
    public Vector2 velocity;
    public Rectangle bounds;
    public boolean facingRight = true;
    public float scale = 1.0f;

    // Components
    public StateComponent stateComponent;
    public HealthComponent healthComponent;
    public PhysicsComponent physicsComponent;
    public AnimationComponent animationComponent;
    protected AssetLoader assetLoader;

    public enum State {
        IDLE, WALK, RUN, JUMP, FALL, ATTACK1, ATTACK2, ATTACK3, HURT, DEAD,
        LIGHT_ATTACK, HEAVY_ATTACK, CHARGED, VADERSTRIKE,
        LIGHTNING_BALL_CAST
    }

    public Character(AssetLoader assetLoader, float x, float y, float scale) {
        this.assetLoader = assetLoader;
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.bounds = new Rectangle(x, y, 0, 0);
        this.scale = scale;

        this.stateComponent = new StateComponent(State.IDLE);
        this.healthComponent = new HealthComponent(100);
        this.physicsComponent = new PhysicsComponent(this);
        this.animationComponent = new AnimationComponent(this.assetLoader);
    }

    public abstract void update(float delta);
    protected abstract void setupAnimations();

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = animationComponent.getCurrentFrame(stateComponent.getCurrentState());
        if (currentFrame == null) return;

        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;

        bounds.set(position.x, position.y, frameWidth, frameHeight);

        if (facingRight) {
            batch.draw(currentFrame, position.x, position.y, frameWidth, frameHeight);
        } else {
            batch.draw(currentFrame, position.x + frameWidth, position.y, -frameWidth, frameHeight);
        }
    }

    // **** ADDED drawDebug METHOD ****
    public void drawDebug(ShapeRenderer shapeRenderer) {
        if (!GameConfig.DEBUG_DRAW_BOXES || shapeRenderer == null) return;
        // Use the utility to draw the basic box
        DebugUtils.drawDebugLines(shapeRenderer, this);
        // Player/Enemy subclasses can override this to add more specific debug info
    }
    // *****************************

    public void takeDamage(int amount) {
        if (healthComponent.isAlive()) {
            healthComponent.decreaseHealth(amount);
            if (healthComponent.isAlive()) {
                stateComponent.setState(State.HURT);
                animationComponent.resetStateTimer(State.HURT);
            } else {
                die();
            }
        }
    }

    protected void die() {
        if (stateComponent.getCurrentState() != State.DEAD) {
            Gdx.app.log("Character", "Character Died at " + position);
            stateComponent.setState(State.DEAD);
            velocity.set(0, 0);
            animationComponent.resetStateTimer(State.DEAD);
        }
    }

    public boolean isAnimationFinished(State state) {
        return animationComponent.isAnimationFinished(state);
    }

    public State getCurrentState() {
        return stateComponent.getCurrentState();
    }

    public boolean isAlive() {
        return healthComponent.isAlive();
    }

    public void dispose() {
        animationComponent.dispose();
    }
}
