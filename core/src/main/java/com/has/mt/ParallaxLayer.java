package com.has.mt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {

    private final Texture texture;
    private final float factor;
    private final boolean wrapHorizontally;
    private final boolean wrapVertically;
    private Camera camera;

    public ParallaxLayer(Texture texture, float factor, boolean wrapHorizontally, boolean wrapVertically) {
        this.texture = texture;
        this.factor = factor;
        this.wrapHorizontally = wrapHorizontally;
        this.wrapVertically = wrapVertically;
        this.texture.setWrap(
            wrapHorizontally ? Texture.TextureWrap.Repeat : Texture.TextureWrap.ClampToEdge,
            wrapVertically ? Texture.TextureWrap.Repeat : Texture.TextureWrap.ClampToEdge
        );
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Renders this layer. The image is assumed to be 576×324 and is scaled to fill a 1920×1080 viewport.
     */
    public void render(SpriteBatch batch) {
        if (camera == null) return;

        float originalWidth = 576f;
        float originalHeight = 324f;

        // Scale so that image height matches the viewport height (1080).
        float scale = camera.viewportHeight / originalHeight;
        float scaledWidth = originalWidth * scale;
        float scaledHeight = originalHeight * scale;

        float cameraLeft = camera.position.x - camera.viewportWidth / 2f;
        float cameraRight = camera.position.x + camera.viewportWidth / 2f;

        // Horizontal offset for parallax effect.
        float xOffset = (camera.position.x * factor) % scaledWidth;
        float drawX = cameraLeft - xOffset;
        float drawY = 0; // Pin at bottom

        if (!wrapHorizontally) {
            batch.draw(texture, cameraLeft, drawY, camera.viewportWidth, scaledHeight);
            return;
        }

        while (drawX < cameraRight) {
            batch.draw(texture, drawX, drawY, scaledWidth, scaledHeight);
            drawX += scaledWidth;
        }
    }

    public void dispose() {
        texture.dispose();
    }
}
