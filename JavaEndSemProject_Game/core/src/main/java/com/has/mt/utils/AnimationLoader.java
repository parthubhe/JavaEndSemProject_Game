// src/com/has/mt/utils/AnimationLoader.java
package com.has.mt.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationLoader {

    /**
     * Creates an Animation from a spritesheet Texture.
     * Assumes frames are laid out row by row.
     *
     * @param texture The spritesheet texture.
     * @param cols Number of columns.
     * @param rows Number of rows.
     * @param frameDuration Duration of each frame.
     * @return The created Animation object.
     */
    public static Animation<TextureRegion> createAnimation(Texture texture, int cols, int rows, float frameDuration) {
        if (texture == null || cols <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Invalid texture or dimensions for animation creation.");
        }

        TextureRegion[][] tmp = TextureRegion.split(texture,
            texture.getWidth() / cols,
            texture.getHeight() / rows);

        Array<TextureRegion> frames = new Array<>(cols * rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames.add(tmp[i][j]);
            }
        }

        return new Animation<>(frameDuration, frames);
    }
}

