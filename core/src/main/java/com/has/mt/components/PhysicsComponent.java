package com.has.mt.components;

import com.badlogic.gdx.math.Vector2;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Character;

public class PhysicsComponent {
    private Character character;
    private boolean onGround = false;
    private float gravity = GameConfig.GRAVITY;

    public PhysicsComponent(Character character) {
        if (character == null) {
            throw new IllegalArgumentException("Character cannot be null for PhysicsComponent");
        }
        this.character = character;
    }

    public void update(float delta) {
        if (character.velocity == null || character.position == null || character.bounds == null) return; // Safety check

        // Apply gravity if not on ground (or if velocity is upward)
        if (!onGround || character.velocity.y > 0) {
            character.velocity.y += gravity * delta;
        }

        // Apply velocity to position
        character.position.x += character.velocity.x * delta;
        character.position.y += character.velocity.y * delta;

        // Check for ground collision (simple version)
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
        // Character.render should update width/height
        character.bounds.setPosition(character.position.x, character.position.y);
    }

    public void jump(float jumpVelocity) {
        if (onGround) {
            if (character.velocity == null) return; // Safety check
            character.velocity.y = jumpVelocity;
            onGround = false;
        }
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void reset() {
        onGround = false;
    }
}
