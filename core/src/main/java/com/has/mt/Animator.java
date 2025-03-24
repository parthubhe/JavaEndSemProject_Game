package com.has.mt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Manages multiple animations (idle, walk, jump, run, attacks, lightning strike, hurt, dead)
 * and their individual timers.
 */
public class Animator {

    public enum State {
        IDLE,
        WALK,
        JUMP,
        RUN,
        LIGHT_ATTACK,
        HEAVY_ATTACK,
        CHARGED,
        LIGHTNING_BALL,
        VADERSTRIKE,
        HURT,
        DEAD
    }

    // Animation objects
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> lightAttackAnimation;
    private Animation<TextureRegion> heavyAttackAnimation;
    private Animation<TextureRegion> chargedAnimation;
    private Animation<TextureRegion> lightningBallAnimation;
    private Animation<TextureRegion> vaderStrikeAnimation;
    private Animation<TextureRegion> hurtAnimation;
    private Animation<TextureRegion> deadAnimation;

    // Animation timers
    private float idleTime = 0f;
    private float walkTime = 0f;
    private float jumpTime = 0f;
    private float runTime = 0f;
    private float lightAttackTime = 0f;
    private float heavyAttackTime = 0f;
    // chargedTime now measures the total time since entering CHARGED state.
    private float chargedTime = 0f;
    private float lightningBallTime = 0f;
    private float vaderStrikeTime = 0f;
    private float hurtTime = 0f;
    private float deadTime = 0f;

    // Textures for disposal
    private Texture idleTexture;
    private Texture walkTexture;
    private Texture jumpTexture;
    private Texture runTexture;
    private Texture lightAttackTexture;
    private Texture heavyAttackTexture;
    private Texture chargedTexture;
    private Texture lightningBallTexture;
    private Texture vaderStrikeTexture;
    private Texture hurtTexture;
    private Texture deadTexture;

    public Animator() {
        // 1) IDLE animation: "LM_Idle.png" (example: 7 frames in 1 row)
        int idleCols = 7, idleRows = 1;
        idleTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Idle.png"));
        TextureRegion[][] idleTemp = TextureRegion.split(idleTexture,
            idleTexture.getWidth() / idleCols,
            idleTexture.getHeight() / idleRows);
        TextureRegion[] idleFrames = new TextureRegion[idleCols];
        for (int i = 0; i < idleCols; i++) {
            idleFrames[i] = idleTemp[0][i];
        }
        idleAnimation = new Animation<>(0.15f, idleFrames);

        // 2) WALK animation: "LM_Walk.png" (example: 7 frames in 1 row)
        int walkCols = 7, walkRows = 1;
        walkTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Walk.png"));
        TextureRegion[][] walkTemp = TextureRegion.split(walkTexture,
            walkTexture.getWidth() / walkCols,
            walkTexture.getHeight() / walkRows);
        TextureRegion[] walkFrames = new TextureRegion[walkCols];
        for (int i = 0; i < walkCols; i++) {
            walkFrames[i] = walkTemp[0][i];
        }
        walkAnimation = new Animation<>(0.1f, walkFrames);

        // 3) JUMP animation: "LM_Jump.png" (example: 8 frames in 1 row)
        int jumpCols = 8, jumpRows = 1;
        jumpTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Jump.png"));
        TextureRegion[][] jumpTemp = TextureRegion.split(jumpTexture,
            jumpTexture.getWidth() / jumpCols,
            jumpTexture.getHeight() / jumpRows);
        TextureRegion[] jumpFrames = new TextureRegion[jumpCols];
        for (int i = 0; i < jumpCols; i++) {
            jumpFrames[i] = jumpTemp[0][i];
        }
        jumpAnimation = new Animation<>(0.1f, jumpFrames);

        // 4) RUN animation: "LM_Run.png" (example: 8 frames in 1 row)
        int runCols = 8, runRows = 1;
        runTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Run.png"));
        TextureRegion[][] runTemp = TextureRegion.split(runTexture,
            runTexture.getWidth() / runCols,
            runTexture.getHeight() / runRows);
        TextureRegion[] runFrames = new TextureRegion[runCols];
        for (int i = 0; i < runCols; i++) {
            runFrames[i] = runTemp[0][i];
        }
        runAnimation = new Animation<>(0.1f, runFrames);

        // 5) LIGHT ATTACK animation: "LM_LightAttack.png" (example: 10 frames in 1 row)
        int lightAttackCols = 10, lightAttackRows = 1;
        lightAttackTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_LightAttack.png"));
        TextureRegion[][] lightAttackTemp = TextureRegion.split(lightAttackTexture,
            lightAttackTexture.getWidth() / lightAttackCols,
            lightAttackTexture.getHeight() / lightAttackRows);
        TextureRegion[] lightAttackFrames = new TextureRegion[lightAttackCols];
        for (int i = 0; i < lightAttackCols; i++) {
            lightAttackFrames[i] = lightAttackTemp[0][i];
        }
        lightAttackAnimation = new Animation<>(0.1f, lightAttackFrames);

        // 6) HEAVY ATTACK animation: "LM_HeavyAttack.png" (example: 4 frames in 1 row)
        int heavyAttackCols = 4, heavyAttackRows = 1;
        heavyAttackTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_HeavyAttack.png"));
        TextureRegion[][] heavyAttackTemp = TextureRegion.split(heavyAttackTexture,
            heavyAttackTexture.getWidth() / heavyAttackCols,
            heavyAttackTexture.getHeight() / heavyAttackRows);
        TextureRegion[] heavyAttackFrames = new TextureRegion[heavyAttackCols];
        for (int i = 0; i < heavyAttackCols; i++) {
            heavyAttackFrames[i] = heavyAttackTemp[0][i];
        }
        heavyAttackAnimation = new Animation<>(0.1f, heavyAttackFrames);

        // 7) CHARGED animation: "LM_Chargeball.png" (example: 7 frames in 1 row)
        int chargedCols = 7, chargedRows = 1;
        chargedTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Chargeball.png"));
        TextureRegion[][] chargedTemp = TextureRegion.split(chargedTexture,
            chargedTexture.getWidth() / chargedCols,
            chargedTexture.getHeight() / chargedRows);
        TextureRegion[] chargedFrames = new TextureRegion[chargedCols];
        for (int i = 0; i < chargedCols; i++) {
            chargedFrames[i] = chargedTemp[0][i];
        }
        chargedAnimation = new Animation<>(0.1f, chargedFrames);

        // 8) LIGHTNING BALL animation: "LM_Charge.png" (example: 9 frames in 1 row)
        int lightningBallCols = 9, lightningBallRows = 1;
        lightningBallTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Charge.png"));
        TextureRegion[][] lightningBallTemp = TextureRegion.split(lightningBallTexture,
            lightningBallTexture.getWidth() / lightningBallCols,
            lightningBallTexture.getHeight() / lightningBallRows);
        TextureRegion[] lightningBallFrames = new TextureRegion[lightningBallCols];
        for (int i = 0; i < lightningBallCols; i++) {
            lightningBallFrames[i] = lightningBallTemp[0][i];
        }
        lightningBallAnimation = new Animation<>(0.1f, lightningBallFrames);

        // 9) HURT animation: "LM_Hurt.png" (example: 3 frames in 1 row)
        int hurtCols = 3, hurtRows = 1;
        hurtTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Hurt.png"));
        TextureRegion[][] hurtTemp = TextureRegion.split(hurtTexture,
            hurtTexture.getWidth() / hurtCols,
            hurtTexture.getHeight() / hurtRows);
        TextureRegion[] hurtFrames = new TextureRegion[hurtCols];
        for (int i = 0; i < hurtCols; i++) {
            hurtFrames[i] = hurtTemp[0][i];
        }
        hurtAnimation = new Animation<>(0.1f, hurtFrames);

        // 10) DEAD animation: "LM_Dead.png" (example: 5 frames in 1 row)
        int deadCols = 5, deadRows = 1;
        deadTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_Dead.png"));
        TextureRegion[][] deadTemp = TextureRegion.split(deadTexture,
            deadTexture.getWidth() / deadCols,
            deadTexture.getHeight() / deadRows);
        TextureRegion[] deadFrames = new TextureRegion[deadCols];
        for (int i = 0; i < deadCols; i++) {
            deadFrames[i] = deadTemp[0][i];
        }
        deadAnimation = new Animation<>(0.1f, deadFrames);

        // 11) VADERSTRIKE animation: "LM_VaderStrike.png" (example: 13 frames in 1 row)
        int vaderStrikeCols = 13, vaderStrikeRows = 1;
        vaderStrikeTexture = new Texture(Gdx.files.internal("Movement/Walk/LM_VaderStrike.png"));
        TextureRegion[][] vaderStrikeTemp = TextureRegion.split(vaderStrikeTexture,
            vaderStrikeTexture.getWidth() / vaderStrikeCols,
            vaderStrikeTexture.getHeight() / vaderStrikeRows);
        TextureRegion[] vaderStrikeFrames = new TextureRegion[vaderStrikeCols];
        for (int i = 0; i < vaderStrikeCols; i++) {
            vaderStrikeFrames[i] = vaderStrikeTemp[0][i];
        }
        vaderStrikeAnimation = new Animation<>(0.1f, vaderStrikeFrames);
    }

    /**
     * Updates the animation timer for a given state.
     */
    public void update(State state, float delta) {
        switch (state) {
            case IDLE:            idleTime += delta;      break;
            case WALK:            walkTime += delta;      break;
            case JUMP:            jumpTime += delta;      break;
            case RUN:             runTime += delta;       break;
            case LIGHT_ATTACK:    lightAttackTime += delta;  break;
            case HEAVY_ATTACK:    heavyAttackTime += delta;  break;
            case CHARGED:         chargedTime += delta;   break;
            case LIGHTNING_BALL:  lightningBallTime += delta; break;
            case VADERSTRIKE:     vaderStrikeTime += delta; break;
            case HURT:            hurtTime += delta;      break;
            case DEAD:            deadTime += delta;      break;
        }
    }

    /**
     * Returns the current frame for the given state.
     * For the CHARGED state, we clamp the time to two loops of the animation.
     */
    public TextureRegion getCurrentFrame(State state) {
        switch (state) {
            case IDLE:
                return idleAnimation.getKeyFrame(idleTime, true);
            case WALK:
                return walkAnimation.getKeyFrame(walkTime, true);
            case JUMP:
                return jumpAnimation.getKeyFrame(jumpTime, false);
            case RUN:
                return runAnimation.getKeyFrame(runTime, true);
            case LIGHT_ATTACK:
                return lightAttackAnimation.getKeyFrame(lightAttackTime, false);
            case HEAVY_ATTACK:
                return heavyAttackAnimation.getKeyFrame(heavyAttackTime, false);
            case CHARGED: {
                // Clamp chargedTime to 2 loops of the animation.
                float loopDuration = chargedAnimation.getAnimationDuration();
                float effectiveTime = Math.min(chargedTime, loopDuration * 2);
                return chargedAnimation.getKeyFrame(effectiveTime, false);
            }
            case LIGHTNING_BALL:
                return lightningBallAnimation.getKeyFrame(lightningBallTime, false);
            case VADERSTRIKE:
                return vaderStrikeAnimation.getKeyFrame(vaderStrikeTime, false);
            case HURT:
                return hurtAnimation.getKeyFrame(hurtTime, false);
            case DEAD:
                return deadAnimation.getKeyFrame(deadTime, false);
        }
        return null;
    }

    /**
     * Returns the current frame of the lightning ball animation.
     */
    public TextureRegion getLightningBallFrame(float time) {
        return lightningBallAnimation.getKeyFrame(time, false);
    }

    /**
     * Reset methods so animations start from frame 0.
     */
    public void resetJump() {
        jumpTime = 0f;
    }
    public void resetLightAttack() {
        lightAttackTime = 0f;
    }
    public void resetHeavyAttack() {
        heavyAttackTime = 0f;
    }
    public void resetVaderStrike() {
        vaderStrikeTime = 0f;
    }
    /**
     * Reset the CHARGED timer.
     * Now the CHARGED animation will automatically end after playing 2 loops.
     */
    public void resetCharged() {
        chargedTime = 0f;
    }

    /**
     * Dispose of all textures.
     */
    public void dispose() {
        idleTexture.dispose();
        walkTexture.dispose();
        jumpTexture.dispose();
        runTexture.dispose();
        lightAttackTexture.dispose();
        heavyAttackTexture.dispose();
        chargedTexture.dispose();
        lightningBallTexture.dispose();
        vaderStrikeTexture.dispose();
        hurtTexture.dispose();
        deadTexture.dispose();
    }
}
