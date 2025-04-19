// src/com/has/mt/level/background/FloorLayer.java
package com.has.mt.level.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color; // Import Color
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;

public class FloorLayer implements Disposable {

    private Texture tileTexture;
    private int tileWidth = 16;
    private int tileHeight = 16;
    private float scale = GameConfig.FLOOR_TILE_SCALE; // Use config
    private float parallaxFactor = GameConfig.FLOOR_PARALLAX_FACTOR; // Use config
    private Texture fullSheetTexture; // Keep reference to dispose


    private boolean loadedDirectly = false; // Flag to know if we need to dispose it

    public FloorLayer(AssetLoader assetLoader, int tileIndex) {
        Gdx.app.log("FloorLayer", "Creating floor for tile index: " + tileIndex);

        // --- TEMPORARY DEBUG: Load Directly ---
        String sheetPath = AssetLoader.FLOOR_TEXTURE_SHEET; // Get path string
        Gdx.app.log("FloorLayer", "Attempting DIRECT load for: " + sheetPath);
        try {
            this.fullSheetTexture = new Texture(Gdx.files.internal(sheetPath));
            this.loadedDirectly = true; // Mark that we loaded it
            Gdx.app.log("FloorLayer", "DIRECT load successful for floor sheet.");
        } catch (Exception e) {
            Gdx.app.error("FloorLayer", "DIRECT load FAILED for floor sheet: " + sheetPath, e);
            // Fallback: Try AssetLoader one last time? Or just fail.
            try {
                Gdx.app.log("FloorLayer", "Falling back to AssetLoader for floor sheet...");
                this.fullSheetTexture = assetLoader.get(sheetPath, Texture.class);
                this.loadedDirectly = false;
            } catch (Exception e2){
                Gdx.app.error("FloorLayer", "Fallback AssetLoader get() also failed for floor sheet.", e2);
                this.fullSheetTexture = null;
            }
        }
        // --- END TEMPORARY DEBUG ---


        if (fullSheetTexture == null) {
            Gdx.app.error("FloorLayer", "Floor texture sheet is NULL after all loading attempts!");
            return; // Cannot create tile texture
        }
        setFloorTileIndex(tileIndex); // Proceed only if texture loaded
    }

    // --- setFloorTileIndex method remains the same ---
    private void setFloorTileIndex(int tileIndex) { /* ... implementation ... */
        if (tileTexture != null) { tileTexture.dispose(); tileTexture = null; }
        if (fullSheetTexture == null) { Gdx.app.error("FloorLayer", "Full sheet texture invalid for setting tile index."); return; }
        try {
            if (!fullSheetTexture.getTextureData().isPrepared()) fullSheetTexture.getTextureData().prepare();
            Pixmap sheetPixmap = fullSheetTexture.getTextureData().consumePixmap();
            int srcX = tileIndex * tileWidth; int srcY = 8 * tileHeight;
            srcX = Math.max(0, Math.min(srcX, sheetPixmap.getWidth() - tileWidth));
            srcY = Math.max(0, Math.min(srcY, sheetPixmap.getHeight() - tileHeight));
            Pixmap tilePixmap = new Pixmap(tileWidth, tileHeight, sheetPixmap.getFormat());
            tilePixmap.drawPixmap(sheetPixmap, 0, 0, srcX, srcY, tileWidth, tileHeight);
            tileTexture = new Texture(tilePixmap);
            tilePixmap.dispose();
            if(tileTexture != null){ tileTexture.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge); tileTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest); Gdx.app.log("FloorLayer", "Tile loaded successfully for index: " + tileIndex); }
            else { Gdx.app.error("FloorLayer", "Failed to create tile texture for index: " + tileIndex); }
        } catch (Exception e) { Gdx.app.error("FloorLayer", "Error extracting tile at index " + tileIndex, e); if (tileTexture != null) { tileTexture.dispose(); tileTexture = null; } }
    }


    // --- render method remains the same ---
    public void render(SpriteBatch batch, float cameraX) { /* ... implementation ... */
        if (tileTexture == null) return;
        Color oldColor = batch.getColor().cpy(); batch.setColor(Color.WHITE);
        float scaledW = tileWidth * scale; float scaledH = tileHeight * scale;
        float camLeft = cameraX - GameConfig.V_WIDTH / 2f; float worldWidth = GameConfig.V_WIDTH; float topY = GameConfig.GROUND_Y;
        float uOffset = (cameraX * parallaxFactor) / scaledW;
        for (float drawY = 0; drawY < topY; drawY += scaledH) {
            float currentStripHeight = Math.min(scaledH, topY - drawY); if (currentStripHeight <= 0) continue;
            float u1 = uOffset; float u2 = uOffset + (worldWidth / scaledW);
            float v1 = 0f; float v2 = currentStripHeight / scaledH;
            batch.draw( tileTexture, camLeft, drawY, worldWidth, currentStripHeight, u1, v2, u2, v1 );
        }
        batch.setColor(oldColor);
    }


    // --- FIX: Dispose texture if loaded directly ---
    @Override
    public void dispose() {
        Gdx.app.log("FloorLayer", "Disposing FloorLayer");
        if (tileTexture != null) {
            tileTexture.dispose(); // Dispose the single tile texture
        }
        // Dispose the full sheet ONLY if we loaded it directly here
        if (loadedDirectly && fullSheetTexture != null) {
            Gdx.app.log("FloorLayer", "Disposing directly loaded full sheet texture.");
            fullSheetTexture.dispose();
        }
        tileTexture = null;
        fullSheetTexture = null;
    }
}
