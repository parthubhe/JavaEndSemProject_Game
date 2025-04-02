package com.has.mt.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.has.mt.gameobjects.Projectile;

public class ProjectileManager implements Disposable {
    private final Array<Projectile> activeProjectiles = new Array<>();

    public void addProjectile(Projectile projectile) {
        if (projectile != null && projectile.isActive()) { // Ensure added projectile is active
            activeProjectiles.add(projectile);
        }
    }
    public void update(float delta) {
        if (activeProjectiles == null) return; // Safety check
        // Iterate backwards for safe removal
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            Projectile p = activeProjectiles.get(i);
            if (p == null) { // Safety check for null elements
                activeProjectiles.removeIndex(i);
                continue;
            }
            p.update(delta); // Update position, lifetime
            if (!p.isActive()) { // Check if projectile became inactive (e.g., lifetime expired or hit something)
                activeProjectiles.removeIndex(i);
                p.dispose(); // Dispose projectile resources
            }
        }
    }
    public void render(SpriteBatch batch) {
        if (activeProjectiles == null || batch == null) return; // Safety checks
        for (Projectile p : activeProjectiles) {
            if (p != null) { // Safety check
                p.render(batch);
            }
        }
    }
    public Array<Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }
    @Override
    public void dispose() {
        if (activeProjectiles != null) {
            for (Projectile p : activeProjectiles) {
                if (p != null) {
                    p.dispose();
                }
            }
            activeProjectiles.clear();
        }
    }
}
