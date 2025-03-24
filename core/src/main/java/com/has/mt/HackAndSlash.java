package com.has.mt;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle; // for bounding-box overlap
import java.util.ArrayList;
import java.util.HashMap;

public class HackAndSlash extends ApplicationAdapter {

    private SpriteBatch spriteBatch;
    private Animator animator;

    // Viewport manager for a 1920×1080 virtual resolution.
    private BackgroundViewportManager viewportManager;

    // The parallax background system.
    private WinterLevelParallax winterParallax;

    // Player animation state.
    private Animator.State currentState;

    // Player position and movement.
    private float x, y;
    private float speed = 200f;
    private float runSpeed = 300f;

    // Jump physics.
    private boolean isJumping = false;
    private float jumpVelocity = 300f;
    private float currentJumpVelocity = 0f;
    private float gravity = -600f;

    // Ground level.
    private float groundY = 50;

    // Facing direction (true = right).
    private boolean facingRight = true;

    // Attack state.
    private boolean inAttack = false;
    private float attackElapsed = 0f;
    private final float LIGHT_ATTACK_DURATION = 1.0f;
    private final float HEAVY_ATTACK_DURATION = 0.4f;
    private final float CHARGED_DURATION = 1.4f;
    private final float VADERSTRIKE_DURATION = 1.2f;

    // Lightning ball projectile.
    private boolean lightningBallActive = false;
    private float projectileTime = 0f;
    private float lightningBallX, lightningBallY;
    private float lightningBallSpeed = 400f;

    // --- Enemy Integration ---
    private ArrayList<Enemy> enemies;
    // Each enemy has an attack cooldown so it doesn't damage the player every frame.
    private HashMap<Enemy, Float> enemyAttackCooldownMap;
    // New: Each enemy also has a damage timer to delay when damage is applied.
    private HashMap<Enemy, Float> enemyDamageTimerMap;
    // Delay before enemy attack "lands" (in seconds)
    private final float ENEMY_ATTACK_DELAY = 0.5f;

    // --- Player Health ---
    private int playerHealth = 100;
    private boolean isPlayerDead = false;

    // For drawing health bars, bounding boxes, etc.
    private ShapeRenderer shapeRenderer;

    // For spawning enemies per background change.
    private int currentBgIndex = 0;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        animator = new Animator();

        // Create viewport manager for 1920×1080.
        viewportManager = new BackgroundViewportManager(1920, 1080);

        // Instantiate the parallax background, passing the viewport's camera.
        winterParallax = new WinterLevelParallax(viewportManager.getCamera());
        winterParallax.create();

        // Initialize player position.
        // Changed spawn x to 3840 so the player appears within the visible area.
        x = 3840;
        y = groundY;
        currentState = Animator.State.IDLE;

        // Initialize enemies (using your previous working Enemy.java).
        enemies = new ArrayList<>();
        enemies.add(new Enemy(600, groundY, "Blue_Slime"));
        enemies.add(new Enemy(900, groundY, "Green_Slime"));

        // Initialize enemy attack cooldown map.
        enemyAttackCooldownMap = new HashMap<>();
        // Initialize enemy damage timer map.
        enemyDamageTimerMap = new HashMap<>();
        for (Enemy enemy : enemies) {
            enemyAttackCooldownMap.put(enemy, 0f);
            enemyDamageTimerMap.put(enemy, 0f);
        }

        // Initialize shape renderer for HUD.
        shapeRenderer = new ShapeRenderer();

        // Reset player's health and dead state.
        playerHealth = 100;
        isPlayerDead = false;
    }

    @Override
    public void resize(int width, int height) {
        viewportManager.resize(width, height);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // --- Handle player death and game reset ---
        if (playerHealth <= 0) {
            isPlayerDead = true;
            currentState = Animator.State.DEAD;
        }
        if (isPlayerDead) {
            animator.update(currentState, delta);
            renderScene(delta);
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                resetGame();
            }
            return;
        }

        // --- Movement, jump, and attack inputs (only if not attacking or casting lightning ball) ---
        if (!inAttack && !lightningBallActive) {
            boolean moving = false;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                x -= speed * delta;
                moving = true;
                facingRight = false;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                x += speed * delta;
                moving = true;
                facingRight = true;
            }

            // Running: W + LSHIFT
            if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    x -= runSpeed * delta;
                    facingRight = false;
                    moving = true;
                } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    x += runSpeed * delta;
                    facingRight = true;
                    moving = true;
                }
                currentState = Animator.State.RUN;
            } else {
                if (!isJumping) {
                    currentState = moving ? Animator.State.WALK : Animator.State.IDLE;
                }
            }

            // Jump
            if (!isJumping && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                isJumping = true;
                currentJumpVelocity = jumpVelocity;
                animator.resetJump();
            }

            // Attacks
            if (Gdx.input.isButtonJustPressed(Buttons.LEFT) || Gdx.input.isButtonJustPressed(Buttons.RIGHT)) {
                currentState = Animator.State.LIGHT_ATTACK;
                inAttack = true;
                attackElapsed = 0f;
                animator.resetLightAttack();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                currentState = Animator.State.HEAVY_ATTACK;
                inAttack = true;
                attackElapsed = 0f;
                animator.resetHeavyAttack();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                currentState = Animator.State.CHARGED;
                inAttack = true;
                attackElapsed = 0f;
                animator.resetCharged();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
                currentState = Animator.State.VADERSTRIKE;
                inAttack = true;
                attackElapsed = 0f;
                animator.resetVaderStrike();
            }
        }

        // --- Apply jump physics ---
        if (isJumping) {
            y += currentJumpVelocity * delta;
            currentJumpVelocity += gravity * delta;
            if (y <= groundY) {
                y = groundY;
                isJumping = false;
            }
            currentState = Animator.State.JUMP;
        }

        // --- Process attack logic and damage to enemies ---
        if (inAttack) {
            attackElapsed += delta;
            if (currentState == Animator.State.LIGHT_ATTACK && attackElapsed >= LIGHT_ATTACK_DURATION) {
                applyDamageToEnemies(15);
                inAttack = false;
                currentState = Animator.State.IDLE;
            } else if (currentState == Animator.State.HEAVY_ATTACK && attackElapsed >= HEAVY_ATTACK_DURATION) {
                applyDamageToEnemies(20);
                inAttack = false;
                currentState = Animator.State.IDLE;
            } else if (currentState == Animator.State.VADERSTRIKE && attackElapsed >= VADERSTRIKE_DURATION) {
                applyDamageToEnemies(30);
                inAttack = false;
                currentState = Animator.State.IDLE;
            } else if (currentState == Animator.State.CHARGED && attackElapsed >= CHARGED_DURATION) {
                inAttack = false;
                currentState = Animator.State.IDLE;
                lightningBallActive = true;
                projectileTime = 0f;
                // Adjust projectile starting position so it appears from the player's hand.
                TextureRegion playerFrame = animator.getCurrentFrame(Animator.State.IDLE);
                float pWidth = playerFrame.getRegionWidth();
                if (facingRight) {
                    lightningBallX = x + pWidth - 10;
                } else {
                    lightningBallX = x + 10;
                }
                lightningBallY = y + pWidth * 0.25f;
            }
        }

        // --- Lightning ball projectile logic ---
        if (lightningBallActive) {
            projectileTime += delta;
            if (facingRight) {
                lightningBallX += lightningBallSpeed * delta;
            } else {
                lightningBallX -= lightningBallSpeed * delta;
            }
            // Only check collision after a short delay so the ball is visible.
            if (projectileTime > 0.2f) {
                for (Enemy enemy : enemies) {
                    if (enemy.getHealth() > 0 && lightningBallCollidesWithEnemy(enemy)) {
                        enemy.takeDamage(10);
                        lightningBallActive = false;
                        projectileTime = 0f;
                        break;
                    }
                }
            }
            float cameraLeft = viewportManager.getCamera().position.x - viewportManager.getCamera().viewportWidth / 2f;
            float cameraRight = viewportManager.getCamera().position.x + viewportManager.getCamera().viewportWidth / 2f;
            if (lightningBallX < cameraLeft - 100f || lightningBallX > cameraRight + 100f) {
                lightningBallActive = false;
                projectileTime = 0f;
            }
        }

        // --- Update animator for the player ---
        animator.update(currentState, delta);
        TextureRegion currentFrame = animator.getCurrentFrame(currentState);

        // --- Update enemies (only if alive) ---
        for (Enemy enemy : enemies) {
            if (enemy.getHealth() > 0) {
                enemy.update(delta, x);
            }
        }

        // --- Enemy attacks on the player (using bounding-box overlap and checking if enemy is attacking) ---
        Rectangle playerRect = new Rectangle(
            x,
            y,
            currentFrame.getRegionWidth() * 3,
            currentFrame.getRegionHeight() * 3
        );
        for (Enemy enemy : enemies) {
            // Update enemy attack cooldown timer.
            float cd = enemyAttackCooldownMap.get(enemy);
            if (cd > 0) {
                enemyAttackCooldownMap.put(enemy, cd - delta);
            }
            // Only consider damage if the enemy is ready and is in its attack animation.
            if (enemy.getHealth() > 0 && enemyAttackCooldownMap.get(enemy) <= 0 && enemy.isAttacking()) {
                float ex = enemy.getX();
                float ey = enemy.getY();
                float ew = enemy.getWidth();
                float eh = enemy.getHeight();
                Rectangle enemyRect = new Rectangle(ex, ey, ew, eh);
                if (playerRect.overlaps(enemyRect)) {
                    // Accumulate enemy damage timer only if overlapping.
                    float attackTime = enemyDamageTimerMap.get(enemy) + delta;
                    enemyDamageTimerMap.put(enemy, attackTime);
                    if (attackTime >= ENEMY_ATTACK_DELAY) {
                        playerHealth -= 10;
                        enemyAttackCooldownMap.put(enemy, 1.5f); // Reset enemy attack cooldown.
                        enemyDamageTimerMap.put(enemy, 0f); // Reset this enemy's damage timer.
                    }
                } else {
                    enemyDamageTimerMap.put(enemy, 0f);
                }
            }
        }

        // --- Update parallax background and check for background change ---
        winterParallax.setCameraX(x);
        winterParallax.update(delta);
        // Check for background change and spawn new enemies if needed.
        int newBgIndex = winterParallax.getCurrentBackgroundIndex();
        if (newBgIndex != currentBgIndex) {
            currentBgIndex = newBgIndex;
            spawnEnemiesForBackground(currentBgIndex);
        }

        // Render the scene.
        renderScene(delta);
    }

    // Render everything.
    private void renderScene(float delta) {
        viewportManager.apply();
        viewportManager.getCamera().update();
        spriteBatch.setProjectionMatrix(viewportManager.getCamera().combined);

        // Clear screen.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update and render parallax background.
        winterParallax.render();

        spriteBatch.begin();
        TextureRegion currentFrame = animator.getCurrentFrame(currentState);
        for (Enemy enemy : enemies) {
            enemy.render(spriteBatch);
        }
        if (facingRight) {
            spriteBatch.draw(
                currentFrame,
                x,
                y,
                currentFrame.getRegionWidth() * 3,
                currentFrame.getRegionHeight() * 3
            );
        } else {
            spriteBatch.draw(
                currentFrame,
                x + currentFrame.getRegionWidth() * 3,
                y,
                -currentFrame.getRegionWidth() * 3,
                currentFrame.getRegionHeight() * 3
            );
        }
        if (lightningBallActive) {
            TextureRegion lbFrame = animator.getLightningBallFrame(projectileTime);
            spriteBatch.draw(
                lbFrame,
                lightningBallX,
                lightningBallY,
                lbFrame.getRegionWidth() * 3,
                lbFrame.getRegionHeight() * 3
            );
        }
        spriteBatch.end();

        // Draw HUD (health bars, debug hitboxes).
        Utils.drawHUD(viewportManager, shapeRenderer, x, y, currentFrame, playerHealth, enemies);
    }

    // Helper to apply damage to enemies in range.
    private void applyDamageToEnemies(int damage) {
        float attackRange = 100f;
        for (Enemy enemy : enemies) {
            if (enemy.getHealth() > 0 && Math.abs(enemy.getX() - x) <= attackRange) {
                enemy.takeDamage(damage);
            }
        }
    }

    // Check bounding-box collision for lightning ball.
    private boolean lightningBallCollidesWithEnemy(Enemy enemy) {
        float ex = enemy.getX();
        float ey = enemy.getY();
        float ew = enemy.getWidth();
        float eh = enemy.getHeight();
        float lbWidth = 30;
        float lbHeight = 30;
        return lightningBallX < ex + ew && lightningBallX + lbWidth > ex &&
            lightningBallY < ey + eh && lightningBallY + lbHeight > ey;
    }

    /**
     * Spawns new enemies when the background changes.
     * New enemy instances are added to the existing enemy list.
     * Note: 7680f is used as the background travel distance so that the entire background stays longer before transitioning.
     */
    private void spawnEnemiesForBackground(int bgIndex) {
        // Calculate the starting X position for this background segment.
        float segmentStartX = bgIndex * 3260f;
        // Choose spawn positions within this segment.
        float spawnX1 = segmentStartX + 600;
        float spawnX2 = segmentStartX + 1200;
        float groundY = 50f; // Assuming same ground level

        // Create new enemy instances and add them to the list.
        Enemy enemy1 = new Enemy(spawnX1, groundY, "Blue_Slime");
        Enemy enemy2 = new Enemy(spawnX2, groundY, "Green_Slime");
        enemies.add(enemy1);
        enemies.add(enemy2);

        // Add new enemy timers.
        enemyAttackCooldownMap.put(enemy1, 0f);
        enemyAttackCooldownMap.put(enemy2, 0f);
        enemyDamageTimerMap.put(enemy1, 0f);
        enemyDamageTimerMap.put(enemy2, 0f);
    }

    // Reset game after player death.
    private void resetGame() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        create();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        animator.dispose();
        winterParallax.dispose();
        shapeRenderer.dispose();
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
    }
}
