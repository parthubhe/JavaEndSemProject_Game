// src/com/has/mt/components/PhysicsComponent.java
package com.has.mt.components;

import com.badlogic.gdx.math.Vector2;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Character;

public class PhysicsComponent {
    private Character character;
    private boolean onGround = false;
    private float gravity = GameConfig.GRAVITY;

    public PhysicsComponent(Character character) {
        this.character = character;
    }

    public void update(float delta) {
        // Apply gravity if not on ground (or if velocity is upward)
        if (!onGround || character.velocity.y > 0) {
            character.velocity.y += gravity * delta;
        }

        // Apply velocity to position
        character.position.x += character.velocity.x * delta;
        character.position.y += character.velocity.y * delta;

        // Check for ground collision (simple version)
        // TODO: Replace with proper collision detection (Tilemap, CollisionManager)
        if (character.position.y <= GameConfig.GROUND_Y) {
            character.position.y = GameConfig.GROUND_Y;
            if (character.velocity.y < 0) { // Only stop if moving downwards
                character.velocity.y = 0;
            }
            onGround = true;
        } else {
            onGround = false;
        }

        // Update bounds position AFTER position update
        // Width/Height are updated in Character.render based on animation frame
        character.bounds.setPosition(character.position.x, character.position.y);

        // TODO: Add world boundary checks? Wall collisions?
    }

    public void jump(float jumpVelocity) {
        if (onGround) {
            character.velocity.y = jumpVelocity;
            onGround = false; // Immediately leave ground state
        }
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void reset() {
        onGround = false; // Re-evaluate on next update
    }
}
