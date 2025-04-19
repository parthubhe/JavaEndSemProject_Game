// src/com/has/mt/ai/BasicChaseAI.java
package com.has.mt.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Enemy;
import com.has.mt.gameobjects.Character.State; // Import State enum

public class BasicChaseAI implements EnemyAI {

    protected Enemy enemy;
    protected Character target;
    protected float moveSpeed = 150f; // Base speed for this AI
    protected float runSpeed = 250f; // Speed when chasing actively

    public BasicChaseAI(Enemy enemy) {
        this.enemy = enemy;
        // Set default speed if needed enemy.getSpeed()? Or configure here.
    }

    @Override
    public void setTarget(Character target) {
        this.target = target;
    }

    @Override
    public void update(float delta) {
        if (enemy == null || !enemy.isAlive() || target == null || !target.isAlive()) {
            // If no target or enemy/target is dead, just idle
            enemy.velocity.x = 0;
            if (enemy.physicsComponent.isOnGround() && !enemy.stateComponent.isState(State.HURT)) {
                enemy.stateComponent.setState(State.IDLE);
            }
            return;
        }

        // Don't change state if hurt or attacking
        if (enemy.stateComponent.isState(State.HURT) || enemy.isAttacking()) {
            // Maybe stop movement if hurt?
            if (enemy.stateComponent.isState(State.HURT)) enemy.velocity.x = 0;
            return;
        }


        float distanceX = target.position.x - enemy.position.x;
        float distanceY = target.position.y - enemy.position.y; // Consider Y distance?
        float absDistanceX = Math.abs(distanceX);

        // Determine desired direction and state based on distance
        if (absDistanceX <= enemy.getAttackRange()) {
            // Within attack range: Stop and Attack
            enemy.velocity.x = 0;
            // Face the target
            enemy.facingRight = (distanceX > 0);
            // Attempt to attack (checks cooldown internally)
            enemy.attemptAttack();
            // If not attacking (e.g., on cooldown), switch to idle
            if (!enemy.isAttacking() && enemy.physicsComponent.isOnGround()) {
                enemy.stateComponent.setState(State.IDLE);
            }

        } else if (absDistanceX <= enemy.getDetectRange()) {
            // Within detection range but outside attack range: Chase (Run)
            enemy.facingRight = (distanceX > 0);
            enemy.velocity.x = enemy.facingRight ? runSpeed : -runSpeed;
            if (enemy.physicsComponent.isOnGround()) {
                enemy.stateComponent.setState(State.RUN);
            }
            // Simple check to avoid running into walls (needs improvement)
            // if (isNearWall()) { enemy.velocity.x = 0; enemy.stateComponent.setState(State.IDLE);}

        } else {
            // Outside detection range: Idle or Patrol (Basic: Idle)
            enemy.velocity.x = 0;
            if (enemy.physicsComponent.isOnGround()) {
                enemy.stateComponent.setState(State.IDLE);
            }
        }
    }

    // Basic wall check placeholder - replace with real collision data
    private boolean isNearWall() {
        // Needs collision map access
        // Example: Check tile ahead based on facing direction
        return false;
    }

    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        // Draw line to target if debugging AI paths
        if (GameConfig.DEBUG_DRAW_PATHS && target != null && enemy != null) {
            shapeRenderer.setColor(1, 0, 1, 1); // Magenta for AI line
            shapeRenderer.line(enemy.position.x + enemy.bounds.width / 2,
                enemy.position.y + enemy.bounds.height / 2,
                target.position.x + target.bounds.width / 2,
                target.position.y + target.bounds.height / 2);
        }
    }
}
