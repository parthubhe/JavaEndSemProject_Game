package com.has.mt;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;

public class Utils {
    public static final boolean DEBUG_HITBOXES = true;

    /**
     * draws the player health bar, enemy health bar
     */
    public static void drawHUD(
                ShapeRenderer shapeRenderer,
        float playerX, float playerY,
        TextureRegion playerFrame,
        BackgroundViewportManager viewportManager,
        int playerHealth,
        ArrayList<Enemy> enemies
    ) {
        // 1) Draw all health bars (player + enemies) with a FILLED shape
        shapeRenderer.setProjectionMatrix(viewportManager.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // --- Player Health Bar ---
        float playerBarWidth = 100f;
        float playerBarHeight = 10f;
        float playerBarX = playerX + (playerFrame.getRegionWidth() * 3 - playerBarWidth) / 2;
        float playerBarY = playerY + playerFrame.getRegionHeight() * 3 + 5;
        shapeRenderer.setColor(0, 1, 0, 1); // green
        shapeRenderer.rect(playerBarX, playerBarY, playerBarWidth * (playerHealth / 100f), playerBarHeight);

        // --- Enemies' Health Bars ---
        for (Enemy enemy : enemies) {

            float enemyBarWidth = 50f;
            float enemyBarHeight = 8f;
            float enemyBarX = ex + 0f;
            float enemyBarY = ey + 50f; 

            float ex = enemy.getX();
            float ey = enemy.getY();
            int eHealth = enemy.getHealth();


            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(
                enemyBarX,
                enemyBarY,
                enemyBarWidth * (eHealth / 100f),
                enemyBarHeight
            );
        }

        shapeRenderer.end();


        if (DEBUG_HITBOXES) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 0, 0, 1); // red

            // --- Player bounding box ---
            shapeRenderer.rect(
                playerX,
                playerY,
                playerFrame.getRegionWidth() * 3,
                playerFrame.getRegionHeight() * 3
            );

            // --- Enemies' bounding boxes + detection range circles ---
            for (Enemy enemy : enemies) {
                float ey = enemy.getY();
                float ex = enemy.getX();
                float range = enemy.getDetectRange();

                // Outline the enemy's bounding box
                // This uses the enemy's texture width/height. You may want to store them in the Enemy class if needed.
                // For now, let's assume each slime is about frame.getRegionWidth() x frame.getRegionHeight().
                // If your Enemy class has an animator, you can do something similar to get that frame:
                // TextureRegion enemyFrame = enemy.getCurrentFrame(); // hypothetical method
                // shapeRenderer.rect(ex, ey, enemyFrame.getRegionWidth(), enemyFrame.getRegionHeight());

                // Outline the detection range circle
                shapeRenderer.circle(ex + 32, ey + 32, range);
                // Outline the enemy's bounding box
                shapeRenderer.rect(ex, ey, enemy.getWidth() -324, enemy.getHeight() -324);

            }

            shapeRenderer.end();
        }
    }
}
