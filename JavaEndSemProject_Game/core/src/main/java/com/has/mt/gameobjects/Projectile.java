// src/com/has/mt/gameobjects/Projectile.java
package com.has.mt.gameobjects;

import com.badlogic.gdx.Gdx; // **** ADDED IMPORT ****
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
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
    private Texture texture; // Keep ref ONLY if loaded DIRECTLY here, not via AssetLoader.get()

    public Projectile(AssetLoader assetLoader, float x, float y, float vx, float vy, int damage, Character owner,
                      String animPath, int cols, int rows, float frameDuration) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(vx, vy);
        this.damage = damage;
        this.owner = owner;
        this.bounds = new Rectangle(x, y, 0, 0); // Updated in render

        try {
            // Get texture from AssetLoader - DO NOT store ref here if AssetLoader manages it
            Texture projectileTexture = assetLoader.get(animPath, Texture.class);
            if (projectileTexture == null) {
                throw new RuntimeException("Projectile texture failed to load from AssetLoader: " + animPath);
            }
            this.animation = AnimationLoader.createAnimation(projectileTexture, cols, rows, frameDuration);
            this.animation.setPlayMode(Animation.PlayMode.LOOP); // Projectiles usually loop

            // Set initial bounds based on first frame
            TextureRegion frame = animation.getKeyFrame(0);
            bounds.width = frame.getRegionWidth() * scale;
            bounds.height = frame.getRegionHeight() * scale;

        } catch (Exception e) {
            Gdx.app.error("Projectile", "Failed to load animation: " + animPath, e);
            active = false;
        }
    }

    public void update(float delta) {
        if (!active) return;

        lifeTimer += delta;
        if (lifeTimer >= lifeSpan) {
            active = false;
            return;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        bounds.setPosition(position.x, position.y);

        stateTime += delta;
    }

    public void render(SpriteBatch batch) {
        if (!active || animation == null) return;

        TextureRegion currentFrame = animation.getKeyFrame(stateTime);
        if (currentFrame == null) return; // Safety check

        float frameWidth = currentFrame.getRegionWidth() * scale;
        float frameHeight = currentFrame.getRegionHeight() * scale;

        bounds.width = frameWidth;
        bounds.height = frameHeight;

        batch.draw(currentFrame, position.x, position.y, frameWidth, frameHeight);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active && !active) { // Log only when changing from active to inactive
            Gdx.app.log("Projectile", "Projectile deactivated.");
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
        // Clear references if needed
        animation = null;
    }
}
