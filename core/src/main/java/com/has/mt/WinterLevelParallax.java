package com.has.mt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class WinterLevelParallax {
    private SpriteBatch batch;
    private Camera camera; // externally provided camera

    private ParallaxLayer[][] backgrounds;
    private final int totalBackgrounds = 8;

    // Instead of 1920, use a larger travel distance per background.
    private final float backgroundTravelDistance = 3840f; // Increase this value to make each background last longer.

    // Boundaries for the camera center:
    private final float leftBoundary = backgroundTravelDistance / 2f;
    private final float rightBoundary = totalBackgrounds * backgroundTravelDistance - backgroundTravelDistance / 2f;

    // Internal variable to follow player's x-position.
    private float cameraX;

    // One floor texture per background, each with a different tile (from row 8)
    private LevelFloorTexture[] floorTextures;
    private final int[] floorTileIndices = {0, 1, 2, 3, 4, 5, 6, 7};

    public WinterLevelParallax(Camera cam) {
        this.camera = cam;
    }

    public void create() {
        batch = new SpriteBatch();

        // Create floor textures with different tiles.
        floorTextures = new LevelFloorTexture[totalBackgrounds];
        for (int i = 0; i < totalBackgrounds; i++) {
            floorTextures[i] = new LevelFloorTexture();
            floorTextures[i].setFloorTileIndex(floorTileIndices[i]);
        }

        // Load background layers from folders.
        backgrounds = new ParallaxLayer[totalBackgrounds][];
        backgrounds[0] = loadBackground("winter 1", 7);
        backgrounds[1] = loadBackground("winter 2", 6);
        backgrounds[2] = loadBackground("winter 3", 4);
        backgrounds[3] = loadBackground("winter 4", 4);
        backgrounds[4] = loadBackground("winter 5", 11);
        backgrounds[5] = loadBackground("winter 6", 4);
        backgrounds[6] = loadBackground("winter 7", 4);
        backgrounds[7] = loadBackground("winter 8", 4);

        // Set the camera for each layer.
        for (int i = 0; i < totalBackgrounds; i++) {
            for (ParallaxLayer layer : backgrounds[i]) {
                layer.setCamera(camera);
            }
        }
        // Initialize cameraX (usually from the player’s position).
        cameraX = camera.position.x;
    }

    /**
     * Helper method to load a background set.
     * Each image is assumed to be 576×324 and scaled to fill 1920×1080.
     */
    private ParallaxLayer[] loadBackground(String folderName, int numLayers) {
        ParallaxLayer[] layers = new ParallaxLayer[numLayers];
        for (int i = 0; i < numLayers; i++) {
            float factor = 0.2f + 0.15f * i; // parallax factor per layer
            String filePath = "Backgrounds/Winter/Layered/" + folderName + "/" + (i + 1) + ".png";
            Texture texture = new Texture(Gdx.files.internal(filePath));
            layers[i] = new ParallaxLayer(texture, factor, true, false);
        }
        return layers;
    }

    /**
     * Update the player's (camera’s) x-position.
     * The value is clamped between the left and right boundaries.
     */
    public void setCameraX(float newX) {
        cameraX = MathUtils.clamp(newX, leftBoundary, rightBoundary);
    }

    public void update(float delta) {
        // Update the camera's position based on the clamped cameraX.
        camera.position.x = cameraX;
        camera.position.y = camera.viewportHeight / 2f;
        camera.update();
    }

    /**
     * Returns the current background index based on cameraX.
     */
    public int getCurrentBackgroundIndex() {
        float relativeX = cameraX - leftBoundary;
        int index = (int)(relativeX / backgroundTravelDistance);
        return MathUtils.clamp(index, 0, totalBackgrounds - 1);
    }

    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Determine which background index the camera is in.
        float relativeX = cameraX - leftBoundary;
        int currentBackgroundIndex = (int)(relativeX / backgroundTravelDistance);
        currentBackgroundIndex = MathUtils.clamp(currentBackgroundIndex, 0, totalBackgrounds - 1);

        // Calculate cross-fade progress within the current travel distance.
        float progress = 0f;
        int nextBackgroundIndex = currentBackgroundIndex;
        if (currentBackgroundIndex < totalBackgrounds - 1) {
            progress = (relativeX % backgroundTravelDistance) / backgroundTravelDistance;
            nextBackgroundIndex = currentBackgroundIndex + 1;
        }

        if (progress > 0f && currentBackgroundIndex < totalBackgrounds - 1) {
            // Crossfade between current and next background.
            float alphaCurrent = 1f - progress;
            float alphaNext = progress;

            batch.setColor(1, 1, 1, alphaCurrent);
            renderLayers(backgrounds[currentBackgroundIndex]);

            batch.setColor(1, 1, 1, alphaNext);
            renderLayers(backgrounds[nextBackgroundIndex]);

            // Crossfade floor textures.
            float floorBlendAlpha = 0.5f;
            batch.setColor(1, 1, 1, alphaCurrent * floorBlendAlpha);
            floorTextures[currentBackgroundIndex].drawFloorFill(batch, camera, 100f, floorBlendAlpha);

            batch.setColor(1, 1, 1, alphaNext * floorBlendAlpha);
            floorTextures[nextBackgroundIndex].drawFloorFill(batch, camera, 100f, floorBlendAlpha);
        } else {
            // If there is no crossfade needed, just draw the current background.
            batch.setColor(1, 1, 1, 1f);
            renderLayers(backgrounds[currentBackgroundIndex]);
            float floorBlendAlpha = 0.5f;
            floorTextures[currentBackgroundIndex].drawFloorFill(batch, camera, 100f, floorBlendAlpha);
        }
        batch.setColor(1, 1, 1, 1f);
        batch.end();
    }

    /**
     * Helper method to render an array of parallax layers.
     */
    private void renderLayers(ParallaxLayer[] layers) {
        for (ParallaxLayer layer : layers) {
            layer.render(batch);
        }
    }

    public void dispose() {
        batch.dispose();
        for (int i = 0; i < totalBackgrounds; i++) {
            for (ParallaxLayer layer : backgrounds[i]) {
                layer.dispose();
            }
        }
        if (floorTextures != null) {
            for (LevelFloorTexture ft : floorTextures) {
                if (ft != null) {
                    ft.dispose();
                }
            }
        }
    }
}
