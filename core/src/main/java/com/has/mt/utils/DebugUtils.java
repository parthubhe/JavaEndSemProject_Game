// src/com/has/mt/utils/DebugUtils.java
package com.has.mt.utils;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Enemy;
import com.badlogic.gdx.utils.Array;
import com.has.mt.ai.EnemyAI; // Import EnemyAI

public class DebugUtils {

    public static void drawDebugLines(ShapeRenderer renderer, Character character) {
        if (!GameConfig.DEBUG_DRAW_BOXES || character == null || renderer == null || character.bounds == null) return;

        renderer.setColor(1, 0, 0, 1); // Red for bounding box
        renderer.rect(character.bounds.x, character.bounds.y, character.bounds.width, character.bounds.height);

        if (character instanceof Enemy) {
            Enemy enemy = (Enemy) character;
            renderer.setColor(0, 0, 1, 1); // Blue for detection range
            renderer.circle(enemy.position.x + enemy.bounds.width / 2, enemy.position.y + enemy.bounds.height / 2, enemy.getDetectRange());
            renderer.setColor(1, 1, 0, 1); // Yellow for attack range
            renderer.circle(enemy.position.x + enemy.bounds.width / 2, enemy.position.y + enemy.bounds.height / 2, enemy.getAttackRange());

            // Draw AI specific debug info
            // **** FIX: Use public getter ****
            EnemyAI ai = enemy.getAI();
            if (ai != null && GameConfig.DEBUG_DRAW_PATHS) {
                ai.drawDebug(renderer);
            }
            // ******************************
        }
    }

    // Overload for drawing multiple characters
    public static void drawDebugLines(ShapeRenderer renderer, Array<? extends Character> characters) {
        if (!GameConfig.DEBUG_DRAW_BOXES || characters == null || renderer == null) return;
        for (Character c : characters) {
            drawDebugLines(renderer, c);
        }
    }
}
