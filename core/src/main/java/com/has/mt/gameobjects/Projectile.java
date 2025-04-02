package com.has.mt.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException; // Import
import com.has.mt.interfaces.GameExceptionMessages; // Import
import com.has.mt.utils.AnimationLoader;

public class Projectile implements Disposable {
    public Vector2 position;
    public Vector2 velocity;
    public Rectangle bounds;
    private int damage;
    private Character owner; // Who shot this? Player or Enemy?
    private boolean active = true;
    private float lifeTimer = 0f;
    private float lifeSpan = GameConfig.PROJECTILE_LIFESPAN;

    // Animation (optional, could be static texture)
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private float scale = 2.0f; // Scale for the projectile visual


    public Projectile(AssetLoader assetLoader, float x, float y, float vx, float vy, int damage, Character owner,
                      String animPath, int cols, int rows, float frameDuration) {
        if(assetLoader == null) throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AssetLoader in Projectile");
        if(owner == null) Gdx.app.error("Projectile", "Projectile created with null owner!"); // Allow null owner? Log error.

        this.position = new Vector2(x, y);
        this.velocity = new Vector2(vx, vy);
        this.damage = damage;
        this.owner = owner;
        this.bounds = new Rectangle(x, y, 0, 0); // Updated in render

        try {
            Texture projectileTexture = assetLoader.get(animPath, Texture.class); // Will throw if fails now
            this.animation = AnimationLoader.createAnimation(projectileTexture, cols, rows, frameDuration);
            this.animation.setPlayMode(Animation.PlayMode.LOOP); // Projectiles usually loop

            TextureRegion frame = animation.getKeyFrame(0);
            bounds.width = frame.getRegionWidth() * scale;
            bounds.height = frame.getRegionHeight() * scale;

        } catch (Exception e) {
            Gdx.app.error("Projectile", "Failed to load animation: " + animPath, e);
            active = false; // Deactivate if creation failed
        }
    }

    public void update(float delta) {
        if (!active) return;

        lifeTimer += delta;
        if (lifeTimer >= lifeSpan) {
            setActive(false); // Use setter to log deactivation
            return;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        bounds.setPosition(position.x, position.y);

        stateTime += delta;
    }

    public void render(SpriteBatch batch) {
        if (!active || animation == null || batch == null) return;

        TextureRegion currentFrame = animation.getKeyFrame(stateTime);
        if (currentFrame == null) return;

        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;

        // Update bounds size every frame in case animation size changes? Usually not needed for projectiles.
        // bounds.width = frameWidth;
        // bounds.height = frameHeight;

        batch.draw(currentFrame, position.x, position.y, frameWidth, frameHeight);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active && !active) { // Log only when changing from active to inactive
            Gdx.app.debug("Projectile", "Projectile deactivated.");
        }
        this.active = active;
    }

    public int getDamage() {
        return damage;
    }

    public Character getOwner() {
        return owner;
    }

    @Override
    public void dispose() {
        // Texture is managed by AssetLoader, DO NOT dispose here.
        animation = null; // Clear reference
    }
}
