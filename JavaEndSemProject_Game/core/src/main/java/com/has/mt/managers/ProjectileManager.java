
// src/com/has/mt/managers/ProjectileManager.java
package com.has.mt.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.has.mt.gameobjects.Projectile;

public class ProjectileManager implements Disposable {

    private final Array<Projectile> activeProjectiles = new Array<>();
    // Optional: Use pooling for performance if creating many projectiles
    // private final Pool<Projectile> projectilePool = new Pool<Projectile>() { ... };

    public void addProjectile(Projectile projectile) {
        if (projectile != null) {
            activeProjectiles.add(projectile);
        }
    }

    public void update(float delta) {
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            Projectile p = activeProjectiles.get(i);
            p.update(delta);
            if (!p.isActive()) {
                activeProjectiles.removeIndex(i);
                p.dispose(); // Dispose projectile resources
                // projectilePool.free(p); // If using pooling
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Projectile p : activeProjectiles) {
            p.render(batch);
        }
    }

    public Array<Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    @Override
    public void dispose() {
        for (Projectile p : activeProjectiles) {
            p.dispose();
        }
        activeProjectiles.clear();
    }
}
