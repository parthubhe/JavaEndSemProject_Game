package com.has.mt.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * An Image widget that displays an animation.
 */
public class AnimatedImage extends Image {
    private final Animation<TextureRegion> animation;
    private float stateTime = 0;
    private TextureRegionDrawable drawable;

    public AnimatedImage(Animation<TextureRegion> animation) {
        super(animation.getKeyFrame(0)); // Set initial frame
        this.animation = animation;
        this.drawable = new TextureRegionDrawable();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        // Update the drawable with the current animation frame
        drawable.setRegion(animation.getKeyFrame(stateTime));
        setDrawable(drawable);
    }

    // Override getMinWidth/Height to reflect the animation frame size if needed
    @Override
    public float getMinWidth() {
        return animation.getKeyFrame(0).getRegionWidth();
    }

    @Override
    public float getMinHeight() {
        return animation.getKeyFrame(0).getRegionHeight();
    }
}
