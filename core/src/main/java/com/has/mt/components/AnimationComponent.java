// src/com/has/mt/components/AnimationComponent.java
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
    // --- FIX: Make fields private ---
    private ObjectMap<Character.State, Animation<TextureRegion>> animations;
    private ObjectMap<Character.State, Float> stateTimers;
    // --------------------------------
    // loadedTextures was only used internally for dispose logic which is now handled by AssetLoader
    // private ObjectMap<Character.State, Texture> loadedTextures; // Can likely be removed

    public AnimationComponent(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
        this.animations = new ObjectMap<>();
        this.stateTimers = new ObjectMap<>();
        // this.loadedTextures = new ObjectMap<>(); // Likely remove
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
            // loadedTextures.put(state, texture); // Remove if not needed
            Gdx.app.log("AnimationComponent", "Loaded animation for state: " + state + " from " + texturePath);
        } catch (Exception e) {
            Gdx.app.error("AnimationComponent", "Failed to load animation for state: " + state + " from path: " + texturePath, e);
        }
    }

    // --- FIX: Add methods to check/link animations ---

    /**
     * Checks if an animation has been added for the given state.
     * @param state The state to check.
     * @return true if an animation exists for the state, false otherwise.
     */
    public boolean hasAnimationForState(Character.State state) {
        return animations.containsKey(state);
    }

    /**
     * Links a target state to use the same animation and timer logic as a source state.
     * Does nothing if the source state animation doesn't exist.
     * Useful for reusing animations (e.g., Fall uses Jump).
     *
     * @param targetState The state to link (e.g., State.FALL).
     * @param sourceState The state whose animation should be used (e.g., State.JUMP).
     * @return true if the link was successful, false otherwise.
     */
    public boolean linkStateAnimation(Character.State targetState, Character.State sourceState) {
        if (animations.containsKey(sourceState)) {
            Animation<TextureRegion> sourceAnim = animations.get(sourceState);
            animations.put(targetState, sourceAnim); // Share the animation object
            // Initialize timer for the target state as well
            if (!stateTimers.containsKey(targetState)) {
                stateTimers.put(targetState, 0f);
            }
            Gdx.app.log("AnimationComponent", "Linked state " + targetState + " to use animation from " + sourceState);
            return true;
        } else {
            Gdx.app.error("AnimationComponent", "Cannot link state " + targetState + ": Source state " + sourceState + " animation not found.");
            return false;
        }
    }
    // -------------------------------------------------


    public void update(Character.State currentState, float delta) {
        if (animations.containsKey(currentState)) {
            float newTime = stateTimers.get(currentState, 0f) + delta;
            stateTimers.put(currentState, newTime);
        }
    }

    public TextureRegion getCurrentFrame(Character.State currentState) {
        Animation<TextureRegion> anim = animations.get(currentState);
        if (anim != null) {
            return anim.getKeyFrame(stateTimers.get(currentState, 0f));
        }
        // Fallback logic (optional): Return idle frame if current state animation missing
        Animation<TextureRegion> idleAnim = animations.get(Character.State.IDLE);
        if (idleAnim != null) {
            // Gdx.app.debug("AnimationComponent", "Warning: No animation for " + currentState + ", returning IDLE frame.");
            return idleAnim.getKeyFrame(0); // Return first frame of idle
        }
        return null; // No animation found at all
    }

    public boolean isAnimationFinished(Character.State state) {
        Animation<TextureRegion> anim = animations.get(state);
        if (anim != null) {
            return anim.isAnimationFinished(stateTimers.get(state, 0f));
        }
        return true; // Consider finished if no animation exists
    }

    public void resetStateTimer(Character.State state) {
        stateTimers.put(state, 0f);
    }

    public float getStateTimer(Character.State state) {
        return stateTimers.get(state, 0f);
    }

    public float getAnimationDuration(Character.State state) {
        Animation<TextureRegion> anim = animations.get(state);
        return (anim != null) ? anim.getAnimationDuration() : 0f;
    }

    @Override
    public void dispose() {
        // Textures are managed by AssetLoader. Just clear maps here.
        Gdx.app.log("AnimationComponent", "Dispose called");
        animations.clear();
        stateTimers.clear();
    }
}
