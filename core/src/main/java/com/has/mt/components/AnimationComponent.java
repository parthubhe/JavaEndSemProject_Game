package com.has.mt.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.has.mt.AssetLoader;
// import com.has.mt.CustomFileNotFoundException; // Not used here directly
import com.has.mt.gameobjects.Character; // Need Character.State enum
import com.has.mt.utils.AnimationLoader;

public class AnimationComponent implements Disposable {
    private AssetLoader assetLoader;
    private ObjectMap<Character.State, Animation<TextureRegion>> animations;
    private ObjectMap<Character.State, Float> stateTimers;


    public AnimationComponent(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
        this.animations = new ObjectMap<>();
        this.stateTimers = new ObjectMap<>();
    }

    public void addAnimation(Character.State state, String texturePath, int cols, int rows, float frameDuration, Animation.PlayMode playMode) {
        try {
            Texture texture = assetLoader.get(texturePath, Texture.class);
            if (texture == null) { // Add null check after get()
                Gdx.app.error("AnimationComponent", "Failed to get Texture (null) for state: " + state + " from path: " + texturePath);
                return; // Don't proceed if texture is null
            }
            Animation<TextureRegion> animation = AnimationLoader.createAnimation(texture, cols, rows, frameDuration);
            animation.setPlayMode(playMode);
            animations.put(state, animation);
            stateTimers.put(state, 0f);
            Gdx.app.debug("AnimationComponent", "Loaded animation for state: " + state + " from " + texturePath); // Changed to debug
        } catch (Exception e) {
            Gdx.app.error("AnimationComponent", "Failed to load animation for state: " + state + " from path: " + texturePath, e);
            // Consider throwing a GameLogicException if an animation is critical
        }
    }

    public boolean hasAnimationForState(Character.State state) {
        return animations.containsKey(state);
    }

    public boolean linkStateAnimation(Character.State targetState, Character.State sourceState) {
        if (animations.containsKey(sourceState)) {
            Animation<TextureRegion> sourceAnim = animations.get(sourceState);
            animations.put(targetState, sourceAnim);
            if (!stateTimers.containsKey(targetState)) {
                stateTimers.put(targetState, 0f);
            }
            Gdx.app.debug("AnimationComponent", "Linked state " + targetState + " to use animation from " + sourceState); // Changed to debug
            return true;
        } else {
            Gdx.app.error("AnimationComponent", "Cannot link state " + targetState + ": Source state " + sourceState + " animation not found.");
            return false;
        }
    }

    public void update(Character.State currentState, float delta) {
        if(currentState == null) return; // Safety check
        if (animations.containsKey(currentState)) {
            float newTime = stateTimers.get(currentState, 0f) + delta;
            stateTimers.put(currentState, newTime);
        }
    }

    public TextureRegion getCurrentFrame(Character.State currentState) {
        if(currentState == null) return null; // Safety check
        Animation<TextureRegion> anim = animations.get(currentState);
        if (anim != null) {
            return anim.getKeyFrame(stateTimers.get(currentState, 0f));
        }
        Animation<TextureRegion> idleAnim = animations.get(Character.State.IDLE);
        if (idleAnim != null) {
            Gdx.app.debug("AnimationComponent", "Warning: No animation for " + currentState + ", returning IDLE frame.");
            return idleAnim.getKeyFrame(0);
        }
        return null;
    }

    public boolean isAnimationFinished(Character.State state) {
        if(state == null) return true; // Consider finished if state is null
        Animation<TextureRegion> anim = animations.get(state);
        if (anim != null) {
            return anim.isAnimationFinished(stateTimers.get(state, 0f));
        }
        return true;
    }

    public void resetStateTimer(Character.State state) {
        if(state == null) return; // Safety check
        stateTimers.put(state, 0f);
    }

    public float getStateTimer(Character.State state) {
        if(state == null) return 0f; // Safety check
        return stateTimers.get(state, 0f);
    }

    public float getAnimationDuration(Character.State state) {
        if(state == null) return 0f; // Safety check
        Animation<TextureRegion> anim = animations.get(state);
        return (anim != null) ? anim.getAnimationDuration() : 0f;
    }

    @Override
    public void dispose() {
        Gdx.app.log("AnimationComponent", "Dispose called");
        animations.clear();
        stateTimers.clear();
    }
}
