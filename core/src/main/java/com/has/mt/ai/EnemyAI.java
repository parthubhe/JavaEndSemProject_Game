package com.has.mt.ai;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.Enemy;

public interface EnemyAI {
    void setTarget(Character target);
    void update(float delta);
    void drawDebug(ShapeRenderer shapeRenderer); // For visualizing AI state/path
}
