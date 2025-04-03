// src/com/has/mt/level/background/ParallaxBackground.java
package com.has.mt.level.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color; // Import Color
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader; // AssetLoader still needed? Only if fallback/shared assets used. Remove if not.
import com.has.mt.GameConfig;

import java.util.Comparator;

public class ParallaxBackground implements Disposable {

    // Inner Layer class modified to handle own texture
    private static class Layer implements Disposable {
        Texture texture; // Layer owns its texture now
        float factorX, factorY, scale = 1.0f;
        boolean repeatX, repeatY;

        public Layer(FileHandle textureFile, float factorX, float factorY, boolean repeatX, boolean repeatY) {
            this.factorX = factorX;
            this.factorY = factorY;
            this.repeatX = repeatX;
            this.repeatY = repeatY;
            try {
                this.texture = new Texture(textureFile); // Load texture directly
                Gdx.app.log("ParallaxBackground$Layer", "Loaded texture: " + textureFile.path());
                this.texture.setWrap(
                    repeatX ? Texture.TextureWrap.Repeat : Texture.TextureWrap.ClampToEdge,
                    repeatY ? Texture.TextureWrap.Repeat : Texture.TextureWrap.ClampToEdge
                );
                if (this.texture.getHeight() > 0) {
                    this.scale = GameConfig.V_HEIGHT / this.texture.getHeight();
                }
            } catch (Exception e) {
                Gdx.app.error("ParallaxBackground$Layer", "Failed to load texture: " + textureFile.path(), e);
                this.texture = null; // Ensure texture is null on failure
            }
        }

        public void render(SpriteBatch batch, float cameraX, float cameraY) {
            if (texture == null) return;
            // (Render logic remains the same as previous version)
            float layerWidthScaled = texture.getWidth() * scale;
            float layerHeightScaled = texture.getHeight() * scale;
            float layerX = cameraX * (1 - factorX) - (GameConfig.V_WIDTH / 2f) * (1 - factorX);
            float u = 0, v = 0, u2 = 1, v2 = 1;
            float drawWidth = GameConfig.V_WIDTH; float drawHeight = GameConfig.V_HEIGHT;
            if (repeatX) {
                u = (cameraX * factorX) / layerWidthScaled;
                u2 = u + (GameConfig.V_WIDTH / layerWidthScaled);
                layerX = cameraX - GameConfig.V_WIDTH / 2f;
            }
            // Draw layer at Y=0 (bottom of screen)
            batch.draw(texture, layerX, 0, drawWidth, drawHeight, u, v2, u2, v);
        }

        @Override
        public void dispose() {
            if (texture != null) {
                Gdx.app.log("ParallaxBackground$Layer", "Disposing texture");
                texture.dispose(); // Dispose texture owned by this layer
            }
        }
    } // End inner Layer class

    // AssetLoader might not be needed anymore if all textures loaded directly
    // private AssetLoader assetLoader;
    private Array<Layer> layers;
    private boolean loadedOk = false; // Flag if loading succeeded

    // Constructor simplified - AssetLoader might be removed if not needed elsewhere
    public ParallaxBackground(AssetLoader assetLoader, String theme, int variant) {
        // this.assetLoader = assetLoader; // Remove if not needed
        Gdx.app.log("ParallaxBackground", "Creating background for theme: " + theme + ", variant: " + variant);
        this.layers = new Array<>();
        loadLayers(theme, variant);
    }

    private void loadLayers(String theme, int variant) {
        loadedOk = false; // Assume failure initially
        if (theme == null) {
            Gdx.app.log("ParallaxBackground", "Theme is null, cannot load layers.");
            return;
        }
        String dirPath = "Backgrounds/" + theme + "/Layered/" + theme.toLowerCase() + " " + variant + "/";
        Gdx.app.log("ParallaxBackground", "Attempting to load layers from: " + dirPath);

        try {
            FileHandle dirHandle = Gdx.files.internal(dirPath);
            if (dirHandle.exists() && dirHandle.isDirectory()) {
                FileHandle[] files = dirHandle.list(".png");
                if (files == null || files.length == 0) {
                    Gdx.app.error("ParallaxBackground", "No .png files found in directory: " + dirPath);
                    return;
                }

                Array<FileHandle> sortedFiles = new Array<>(files);
                try {
                    // Sort numerically by filename without extension
                    sortedFiles.sort(Comparator.comparingInt(f -> Integer.parseInt(f.nameWithoutExtension())));
                } catch (NumberFormatException e) {
                    Gdx.app.error("ParallaxBackground", "Could not sort files numerically, sorting alphabetically: " + dirPath, e);
                    sortedFiles.sort(Comparator.comparing(FileHandle::name)); // Fallback sort
                }


                float baseFactor = 0.05f; // Adjust factors as needed
                float factorIncrement = 0.1f; // Adjust factors as needed

                for (int i = 0; i < sortedFiles.size; i++) {
                    FileHandle file = sortedFiles.get(i);
                    if (file.name().equalsIgnoreCase("hd.png")) continue; // Skip hd.png

                    // Load layer directly
                    float factor = baseFactor + (i * factorIncrement);
                    Layer layer = new Layer(file, factor, 0f, true, false);
                    if (layer.texture != null) { // Only add if texture loaded successfully
                        layers.add(layer);
                    } else {
                        Gdx.app.error("ParallaxBackground", "Skipping layer due to texture load failure: " + file.path());
                    }
                }
                if (layers.size > 0) {
                    loadedOk = true; // Mark as successfully loaded if at least one layer added
                    Gdx.app.log("ParallaxBackground", "Finished loading " + layers.size + " layers.");
                } else {
                    Gdx.app.error("ParallaxBackground", "No layers were successfully loaded from: " + dirPath);
                }

            } else {
                Gdx.app.error("ParallaxBackground", "Directory not found or not a directory: " + dirPath);
            }
        } catch (Exception e) {
            Gdx.app.error("ParallaxBackground", "Error accessing/processing directory: " + dirPath, e);
        }
    }

    public void render(SpriteBatch batch, float cameraX) {
        if (!loadedOk || layers == null || layers.size == 0) {
            // Gdx.app.debug("ParallaxBackground", "Skipping render - not loaded or no layers.");
            return; // Don't render if loading failed or no layers
        }
        // --- FIX: Ensure batch color is white before drawing background ---
        Color oldColor = batch.getColor().cpy();
        batch.setColor(Color.WHITE); // Use white for background layers

        for (Layer layer : layers) {
            layer.render(batch, cameraX, GameConfig.V_HEIGHT / 2f);
        }

        batch.setColor(oldColor); // Restore previous color
        // ---------------------------------------------------------------
    }

    @Override
    public void dispose() {
        Gdx.app.log("ParallaxBackground", "Disposing ParallaxBackground layers");
        if (layers != null) {
            for (Layer layer : layers) {
                layer.dispose(); // Each layer disposes its own texture
            }
            layers.clear();
        }
    }
}
