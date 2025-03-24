package com.has.mt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EnemyAnimator {

    public enum State {
        IDLE, WALK, RUN, JUMP, ATTACK_1, ATTACK_2, ATTACK_3, HURT, DEAD
    }

    private Animation<TextureRegion> idleAnimation, walkAnimation, runAnimation, jumpAnimation;
    private Animation<TextureRegion> attack1Animation, attack2Animation, attack3Animation, hurtAnimation, deadAnimation;

    private float idleTime, walkTime, runTime, jumpTime, attack1Time, attack2Time, attack3Time, hurtTime, deadTime;

    private Texture idleTexture, walkTexture, runTexture, jumpTexture;
    private Texture attack1Texture, attack2Texture, attack3Texture, hurtTexture, deadTexture;

    public EnemyAnimator(String path) {
        idleTexture = new Texture(Gdx.files.internal(path + "Idle.png"));
        walkTexture = new Texture(Gdx.files.internal(path + "Walk.png"));
        runTexture = new Texture(Gdx.files.internal(path + "Run.png"));
        jumpTexture = new Texture(Gdx.files.internal(path + "Jump.png"));
        attack1Texture = new Texture(Gdx.files.internal(path + "Attack_1.png"));
        attack2Texture = new Texture(Gdx.files.internal(path + "Attack_2.png"));
        attack3Texture = new Texture(Gdx.files.internal(path + "Attack_3.png"));
        hurtTexture = new Texture(Gdx.files.internal(path + "Hurt.png"));
        deadTexture = new Texture(Gdx.files.internal(path + "Dead.png"));

        idleAnimation = createAnimation(idleTexture, 8, 0.15f);
        walkAnimation = createAnimation(walkTexture, 8, 0.1f);
        runAnimation = createAnimation(runTexture, 7, 0.1f);
        jumpAnimation = createAnimation(jumpTexture, 13, 0.1f);
        attack1Animation = createAnimation(attack1Texture, 4, 0.12f);
        attack2Animation = createAnimation(attack2Texture, 4, 0.12f);
        attack3Animation = createAnimation(attack3Texture, 5, 0.12f);
        hurtAnimation = createAnimation(hurtTexture, 6, 0.1f);
        deadAnimation = createAnimation(deadTexture, 3, 0.15f);
    }

    private Animation<TextureRegion> createAnimation(Texture texture, int frames, float frameDuration) {
        TextureRegion[][] temp = TextureRegion.split(texture, texture.getWidth() / frames, texture.getHeight());
        TextureRegion[] framesArray = new TextureRegion[frames];
        System.arraycopy(temp[0], 0, framesArray, 0, frames);
        return new Animation<>(frameDuration, framesArray);
    }

    public void update(State state, float delta) {
        switch (state) {
            case IDLE -> idleTime += delta;
            case WALK -> walkTime += delta;
            case RUN -> runTime += delta;
            case JUMP -> jumpTime += delta;
            case ATTACK_1 -> attack1Time += delta;
            case ATTACK_2 -> attack2Time += delta;
            case ATTACK_3 -> attack3Time += delta;
            case HURT -> hurtTime += delta;
            case DEAD -> deadTime += delta;
        }
    }

    public TextureRegion getCurrentFrame(State state) {
        return switch (state) {
            case IDLE -> idleAnimation.getKeyFrame(idleTime, true);
            case WALK -> walkAnimation.getKeyFrame(walkTime, true);
            case RUN -> runAnimation.getKeyFrame(runTime, true);
            case JUMP -> jumpAnimation.getKeyFrame(jumpTime, false);
            case ATTACK_1 -> attack1Animation.getKeyFrame(attack1Time, false);
            case ATTACK_2 -> attack2Animation.getKeyFrame(attack2Time, false);
            case ATTACK_3 -> attack3Animation.getKeyFrame(attack3Time, false);
            case HURT -> hurtAnimation.getKeyFrame(hurtTime, false);
            case DEAD -> deadAnimation.getKeyFrame(deadTime, false);
        };
    }

    public void dispose() {
        idleTexture.dispose();
        walkTexture.dispose();
        runTexture.dispose();
        jumpTexture.dispose();
        attack1Texture.dispose();
        attack2Texture.dispose();
        attack3Texture.dispose();
        hurtTexture.dispose();
        deadTexture.dispose();
    }
}
