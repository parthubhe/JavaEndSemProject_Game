package com.has.mt.level.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException; // Import exception
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException; // Import exceptions
import com.has.mt.interfaces.GameExceptionMessages;


public class FloorLayer implements Disposable {

    private Texture tileTexture; // The single extracted tile
    private int tileWidth = 16;
    private int tileHeight = 16;
    private float scale = GameConfig.FLOOR_TILE_SCALE;
    private float parallaxFactor = GameConfig.FLOOR_PARALLAX_FACTOR;
    private Texture fullSheetTexture; // Reference to the sheet from AssetLoader
    private int currentTileIndex = -1;

    // --- REMOVED loadedDirectly flag ---

    public FloorLayer(AssetLoader assetLoader, int tileIndex) {
        Gdx.app.log("FloorLayer", "Creating floor layer for tile index: " + tileIndex);
        if (assetLoader == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AssetLoader in FloorLayer");
        }

        try {
            // Get the pre-loaded texture sheet from the AssetLoader
            this.fullSheetTexture = assetLoader.get(AssetLoader.FLOOR_TEXTURE_SHEET, Texture.class);
            if (this.fullSheetTexture == null) { // Should be caught by assetLoader.get() throwing an exception
                throw new GameLogicException(GameExceptionMessages.ASSET_LOAD_FAILED, AssetLoader.FLOOR_TEXTURE_SHEET + " (result was null)");
            }
            setFloorTileIndex(tileIndex); // Extract the specific tile

        } catch (Exception e) { // Catch exceptions during texture getting or tile extraction
            Gdx.app.error("FloorLayer", "Failed to initialize floor layer", e);
            // Ensure resources are null if init fails
            if (tileTexture != null) tileTexture.dispose();
            tileTexture = null;
            fullSheetTexture = null; // Don't hold reference if failed
            // Re-throw as a runtime exception if floor is critical
            throw new GameLogicException("FloorLayer initialization failed", e);
        }
    }

    private void setFloorTileIndex(int tileIndex) {
        if (tileIndex == currentTileIndex && tileTexture != null) {
            // Gdx.app.debug("FloorLayer", "Tile index " + tileIndex + " already set.");
            return; // Already set to this tile
        }

        // Dispose previous tile texture if it exists
        if (tileTexture != null) {
            tileTexture.dispose();
            tileTexture = null;
        }
        currentTileIndex = -1; // Reset current index

        if (fullSheetTexture == null) {
            Gdx.app.error("FloorLayer", "Cannot set tile index, full sheet texture is null.");
            return;
        }

        Pixmap sheetPixmap = null;
        Pixmap tilePixmap = null;
        try {
            // Ensure texture data is prepared and get Pixmap
            if (!fullSheetTexture.getTextureData().isPrepared()) {
                fullSheetTexture.getTextureData().prepare();
            }
            sheetPixmap = fullSheetTexture.getTextureData().consumePixmap();

            // Calculate source coordinates (assuming 16x16 tiles, row 8)
            // TODO: Make tile size and source row configurable if needed
            int tilesPerRow = fullSheetTexture.getWidth() / tileWidth;
            int srcX = (tileIndex % tilesPerRow) * tileWidth;
            int srcY = 8 * tileHeight; // Hardcoded row 8 (index 8 from top)

            // Clamp coordinates to prevent out-of-bounds errors
            if (srcX < 0 || srcY < 0 || srcX + tileWidth > sheetPixmap.getWidth() || srcY + tileHeight > sheetPixmap.getHeight()) {
                Gdx.app.error("FloorLayer", "Calculated tile coordinates [" + srcX + "," + srcY + "] are out of bounds for sheet size ["+sheetPixmap.getWidth()+"x"+sheetPixmap.getHeight()+"] and tile index " + tileIndex);
                throw new GdxRuntimeException("Tile index out of bounds");
            }


            // Create new pixmap for the single tile
            tilePixmap = new Pixmap(tileWidth, tileHeight, sheetPixmap.getFormat());
            // Copy the tile data from the sheet pixmap to the tile pixmap
            tilePixmap.drawPixmap(sheetPixmap, 0, 0, srcX, srcY, tileWidth, tileHeight);

            // Create the Texture from the tile pixmap
            tileTexture = new Texture(tilePixmap);
            tileTexture.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge); // Repeat horizontally
            tileTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest); // Pixelated look

            currentTileIndex = tileIndex; // Update current index
            Gdx.app.log("FloorLayer", "Floor tile loaded successfully for index: " + tileIndex);

        } catch (Exception e) {
            Gdx.app.error("FloorLayer", "Error extracting floor tile at index " + tileIndex, e);
            if (tileTexture != null) tileTexture.dispose(); // Clean up partially created texture
            tileTexture = null;
            currentTileIndex = -1;
            // Optional: throw exception?
        } finally {
            // Dispose pixmaps if they were created
            if (tilePixmap != null) tilePixmap.dispose();
            // IMPORTANT: DO NOT dispose the sheetPixmap here if you got it via consumePixmap()
            // unless you are absolutely done with the original TextureData. Re-prepare if needed.
            // If you got it via getPixmap(), you would dispose it. Since we used consume, manage carefully.
            // For simplicity here, we assume consumePixmap gives us control and we dispose the tilePixmap.
            // If issues arise, revisit TextureData management.
            if (sheetPixmap != null && fullSheetTexture.getTextureData().disposePixmap()) {
                // Check if the texture data owns the pixmap and dispose it if necessary
                // This might be needed if consumePixmap doesn't transfer ownership fully or for cleanup.
                // sheetPixmap.dispose(); // Careful with this, might invalidate original texture.
            }
        }
    }


    public void render(SpriteBatch batch, float cameraX) {
        if (tileTexture == null || batch == null) return; // Safety checks

        Color oldColor = batch.getColor().cpy();
        batch.setColor(Color.WHITE); // Ensure texture is drawn normally

        float scaledTileWidth = tileWidth * scale;
        float scaledTileHeight = tileHeight * scale;
        float viewportWidth = GameConfig.V_WIDTH; // Use game config width
        float groundY = GameConfig.GROUND_Y; // Use ground level from config

        // Calculate the starting X position for drawing based on camera and viewport
        float drawStartX = cameraX - viewportWidth / 2f;

        // Calculate the texture coordinate offset based on parallax scrolling
        // u-coordinate scrolls based on camera movement and parallax factor
        float uOffset = (cameraX * parallaxFactor) / scaledTileWidth;

        // Draw the floor by tiling vertically up to the ground level
        for (float drawY = 0; drawY < groundY; drawY += scaledTileHeight) {
            float currentStripHeight = Math.min(scaledTileHeight, groundY - drawY);
            if (currentStripHeight <= 0) continue; // Skip if height is zero or negative

            // Calculate texture coordinates (u, v)
            // u/u2 define the horizontal part of the texture to draw (repeats)
            // v/v2 define the vertical part (clamps at the top edge)
            float u1 = uOffset;
            float u2 = uOffset + (viewportWidth / scaledTileWidth);
            float v1 = 0f; // Top of the texture region (corresponds to bottom of tile image)
            // v2 needs to be adjusted based on the strip height relative to the full tile height
            float v2 = currentStripHeight / scaledTileHeight;

            // Draw the texture strip
            // Arguments: texture, x, y, width, height, u, v, u2, v2
            // Note the v/v2 inversion: LibGDX Y-down texture coords vs Y-up world coords
            batch.draw(tileTexture,
                drawStartX, drawY,               // Position (bottom-left corner)
                viewportWidth, currentStripHeight, // Size of the strip to draw
                u1, v2,                         // Texture coords (bottom-left) -> (u1, 1 - v1 adjusted for strip)
                u2, v1);                        // Texture coords (top-right) -> (u2, 1 - v2 adjusted for strip)
            // Correction: LibGDX draw uses u,v bottom-left, u2,v2 top-right.
            // V coordinates range from 0 (top) to 1 (bottom) in the texture file.
            // We want to draw from y=0 up to y=currentStripHeight.
            // So, v should be 0 (top of texture), and v2 should represent the bottom edge of the strip.
            // Let's verify the draw method documentation again.
            // draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2)
            // Draws a rectangle with the texture coordinates specified. The coordinates are relative to the texture size.
            // u,v top-left, u2,v2 bottom-right? NO, documentation says: u,v top-left, u2, v2 bottom right OF THE REGION.
            // Let's rethink the vertical tiling. We draw strips from bottom up.
            // For a strip at drawY, height currentStripHeight:
            // We want the *bottom* part of the texture for strips near y=0, and the *top* part for strips near groundY.
            // Let's assume the tile texture itself represents the full height (16px).
            // If drawing a full height strip (currentStripHeight == scaledTileHeight): v1=0, v2=1.
            // If drawing a partial strip at the top (e.g., groundY is not multiple of scaledTileHeight):
            // The last strip has height currentStripHeight. We need the top 'currentStripHeight/scaledTileHeight' fraction of the texture.
            // So, for the LAST strip: v1=0, v2 = currentStripHeight/scaledTileHeight.
            // For strips BELOW the top: v1=0, v2=1? No, that stretches.
            // OK, let's simplify. Tile the full height texture repeatedly.
            // The `batch.draw` call handles clipping the texture coords correctly if we provide the region to sample.
            // u1, v1 = top-left tex coord; u2, v2 = bottom-right tex coord.
            // For horizontal repeat: u1 = offset, u2 = offset + width_ratio
            // For vertical clamp (drawing bottom part first): v1 = 0 (top of texture), v2 = 1 (bottom of texture) - should be correct.
            // Let's retry the original draw call logic, it might be correct.
            // batch.draw(tileTexture, drawStartX, drawY, viewportWidth, currentStripHeight, u1, v2, u2, v1); // Original trial - seems wrong v/v2 swap
            // Trying standard u/v mapping:
            // batch.draw(tileTexture, drawStartX, drawY, viewportWidth, currentStripHeight, u1, 1f, u2, 0f); // Draws full texture stretched? No.
            // Let's use the draw call specifying source coords:
            // draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY)
            // This seems too complex for simple tiling.

            // Back to the region draw call: draw(Texture region, float x, float y, float width, float height) - draws the whole texture.
            // draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2)
            // Let's assume: u,v = bottom-left texture coord; u2,v2 = top-right texture coord.
            // Texture coords Y=0 is TOP of image, Y=1 is BOTTOM.
            // To draw a strip of height `currentStripHeight` starting at `drawY`:
            // The texture part needed is from V = 1 - (currentStripHeight / scaledTileHeight) to V = 1.
            // Let's try v = 1 - (currentStripHeight / scaledTileHeight) and v2 = 1.

            float v_bottom = 1.0f; // Bottom of texture
            float v_top = 0.0f;    // Top of texture
            float u_left = uOffset;
            float u_right = uOffset + (viewportWidth / scaledTileWidth);
            // Draw the region specified by u/v coordinates
            batch.draw(tileTexture, drawStartX, drawY, viewportWidth, currentStripHeight, u_left, v_bottom, u_right, v_top);
            // This draws the full vertical range of the texture scaled into currentStripHeight. Correct for repeating floor pattern.


        }

        batch.setColor(oldColor); // Restore original batch color
    }


    @Override
    public void dispose() {
        Gdx.app.log("FloorLayer", "Disposing FloorLayer");
        if (tileTexture != null) {
            tileTexture.dispose(); // Dispose the extracted tile texture
            tileTexture = null;
        }
        // The fullSheetTexture is managed by AssetLoader, DO NOT dispose here.
        fullSheetTexture = null; // Release reference
        currentTileIndex = -1;
    }
}
