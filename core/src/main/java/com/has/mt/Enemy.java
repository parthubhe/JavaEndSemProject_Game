package com.has.mt;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Enemy {

    private EnemyAnimator animator;
    private EnemyAnimator.State state;

    private float x, y;
    private float speed = 80f;
    private float attackRange = 100f;
    private float detectRange = 600f;
    private int health = 100;
    private boolean facingRight = false;
    private boolean attacking = false;
    private float attackCooldown = 1.5f;
    private float attackTimer = 0f;

    // CHANGE: Increase scale so enemy is larger on-screen.
    private float scale = 7.0f;

    public Enemy(float x, float y, String type) {
        this.x = x;
        this.y = y;
        String path = "Enemy/SlimeEnemy/" + type + "/";
        animator = new EnemyAnimator(path);
        state = EnemyAnimator.State.IDLE;
    }

    public void update(float delta, float playerX) {
        if (health <= 0) {
            state = EnemyAnimator.State.DEAD;
            return;
        }

        attackTimer += delta;
        float distance = Math.abs(playerX - x);

        if (distance < detectRange) {
            if (distance > attackRange) {
                state = EnemyAnimator.State.RUN;
                x += speed * delta * (playerX > x ? 1 : -1);
                facingRight = (playerX > x);
            } else if (attackTimer >= attackCooldown) {
                attack();
            }
        } else {
            state = EnemyAnimator.State.WALK;
            x += speed * delta * (Math.random() > 0.5 ? 1 : -1);
        }

        animator.update(state, delta);
    }

    private void attack() {
        attacking = true;
        attackTimer = 0f;
        int attackType = (int) (Math.random() * 3);
        state = switch (attackType) {
            case 0 -> EnemyAnimator.State.ATTACK_1;
            case 1 -> EnemyAnimator.State.ATTACK_2;
            default -> EnemyAnimator.State.ATTACK_3;
        };
    }

    public void takeDamage(int dmg) {
        if (health > 0) {
            health -= dmg;
            state = (health > 0) ? EnemyAnimator.State.HURT : EnemyAnimator.State.DEAD;
        }
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame = animator.getCurrentFrame(state);
        float scaledWidth = getWidth();
        float scaledHeight = getHeight();

        if (facingRight) {
            batch.draw(frame, x, y, scaledWidth, scaledHeight);
        } else {
            batch.draw(frame, x + scaledWidth, y, -scaledWidth, scaledHeight);
        }
    }

    public void dispose() {
        animator.dispose();
    }

    // --- Getter Methods ---

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public float getDetectRange() {
        return detectRange;
    }

    public float getWidth() {
        TextureRegion frame = animator.getCurrentFrame(state);
        return frame.getRegionWidth() * scale;
    }

    public float getHeight() {
        TextureRegion frame = animator.getCurrentFrame(state);
        return frame.getRegionHeight() * scale;
    }
    public boolean isAttacking() {
        return state == EnemyAnimator.State.ATTACK_1 ||
            state == EnemyAnimator.State.ATTACK_2 ||
            state == EnemyAnimator.State.ATTACK_3;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
