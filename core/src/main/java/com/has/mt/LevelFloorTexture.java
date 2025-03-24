package com.has.mt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LevelFloorTexture {

    private Texture tileTexture;        // The single tile extracted from row 8
    private int tileWidth = 16;         // The width/height of the single tile
    private int tileHeight = 16;
    private float scale = 2.0f;         // How much to scale each tile
    private float parallaxFactor = 0.3f; // Controls how the texture scrolls horizontally

    /**
     * Called once per background. We'll pick a specific tile from row 8, column = tileIndex.
     */
    public LevelFloorTexture() {
        // By default, pick tile at column 0 (will be overridden by setFloorTileIndex)
        tileTexture = null;
    }

    /**
     * Extract the single tile (row 8, column=tileIndex) from the big sheet.
     * Then set wrapping to Repeat horizontally and ClampToEdge vertically.
     */
    public void setFloorTileIndex(int tileIndex) {
        // If a tileTexture was already created, dispose it first
        if (tileTexture != null) {
            tileTexture.dispose();
            tileTexture = null;
        }

        // Load the big sheet
        Texture bigSheet = new Texture(Gdx.files.internal("FloorTextures/Textures-16.png"));
        // Convert to Pixmap
        bigSheet.getTextureData().prepare();
        Pixmap sheetPixmap = bigSheet.getTextureData().consumePixmap();

        // The row we want is 8 (0-based). The column is tileIndex
        // We'll copy exactly one 16×16 tile.
        int srcX = tileIndex * tileWidth;
        int srcY = 8 * tileHeight;

        Pixmap tilePixmap = new Pixmap(tileWidth, tileHeight, sheetPixmap.getFormat());
        tilePixmap.drawPixmap(
            sheetPixmap,
            0, 0,           // destX, destY
            srcX, srcY,     // srcX, srcY
            tileWidth, tileHeight
        );

        // Create a new texture from this single tile
        tileTexture = new Texture(tilePixmap);
        tilePixmap.dispose();
        sheetPixmap.dispose();
        bigSheet.dispose();

        // Now set wrapping so we can repeat horizontally but clamp vertically
        tileTexture.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
        // Use nearest filter to avoid blending from adjacent columns/rows
        tileTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    }

    /**
     * Draw repeated strips from y=0..topY using the single tile, scaled up, with parallax.
     *
     * @param batch  The SpriteBatch
     * @param camera The Camera
     * @param topY   Up to which Y we fill the floor
     * @param alpha  Transparency
     */
    public void drawFloorFill(SpriteBatch batch, Camera camera, float topY, float alpha) {
        if (tileTexture == null) {
            // If setFloorTileIndex wasn't called, do nothing
            return;
        }

        float oldAlpha = batch.getColor().a;
        batch.setColor(1, 1, 1, alpha);

        // The size of one tile on-screen after scaling
        float scaledW = tileWidth * scale;
        float scaledH = tileHeight * scale;

        // Camera's horizontal span
        float camLeft = camera.position.x - camera.viewportWidth / 2f;
        float camRight = camera.position.x + camera.viewportWidth / 2f;
        float worldWidth = camRight - camLeft;

        // Horizontal parallax offset
        float uOffset = (camera.position.x * parallaxFactor) / scaledW;

        // We'll draw multiple horizontal strips from y=0 up to y=topY
        for (float drawY = 0; drawY < topY; drawY += scaledH) {
            // We repeat horizontally from camLeft..camRight
            // We'll do one big quad, with U range [uOffset..uOffset + (worldWidth/scaledW)]
            float u1 = uOffset;
            float u2 = uOffset + (worldWidth / scaledW);

            // V is always [0..1], since it's a single tile in the vertical direction
            batch.draw(
                tileTexture,
                camLeft, drawY,      // x, y
                worldWidth, scaledH, // width, height
                u1, 0f,  // u, v start
                u2, 1f   // u, v end
            );
        }

        batch.setColor(1, 1, 1, oldAlpha);
    }

    public void dispose() {
        if (tileTexture != null) {
            tileTexture.dispose();
        }
    }
}
