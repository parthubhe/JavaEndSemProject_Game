package com.has.mt;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Helper class to manage a FitViewport for a virtual resolution.
 * This example uses 1920×1080, preserving a 16:9 aspect ratio.
 */
public class BackgroundViewportManager {
    private final OrthographicCamera camera;
    private final Viewport viewport;

    public BackgroundViewportManager(float virtualWidth, float virtualHeight) {
        camera = new OrthographicCamera();
        viewport = new FitViewport(virtualWidth, virtualHeight, camera);
        viewport.apply();
        camera.position.set(virtualWidth / 2f, virtualHeight / 2f, 0);
        camera.update();
    }

    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);
        camera.update();
    }

        public Viewport getViewport() {
        return viewport;
    }


    public OrthographicCamera getCamera() {
        return camera;
    }

        public void apply() {
        viewport.apply();
    }

}
