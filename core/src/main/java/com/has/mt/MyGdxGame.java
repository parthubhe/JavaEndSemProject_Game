// src/com/has/mt/MyGdxGame.java
package com.has.mt;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.has.mt.screens.MainMenuScreen; // Assuming MainMenuScreen is created

public class MyGdxGame extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public AssetLoader assetLoader;
    public DatabaseManager dbManager; // Keep DB manager accessible

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        assetLoader = new AssetLoader();

        // Start loading assets immediately (can add a loading screen later)
        assetLoader.load();
        // Crucial: Block until loading is complete for this basic example
        assetLoader.manager.finishLoading();

        dbManager = DatabaseManager.getInstance(); // Initialize DB

        Gdx.app.log("MyGdxGame", "Assets Loaded. Starting Main Menu.");
        // Set the first screen (e.g., Main Menu)
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // Important! Delegates render to the current screen
    }

    @Override
    public void dispose() {
        Gdx.app.log("MyGdxGame", "Disposing Game Resources.");
        if (screen != null) {
            screen.dispose();
        }
        batch.dispose();
        shapeRenderer.dispose();
        assetLoader.dispose();
        if (dbManager != null) {
            dbManager.close(); // Close DB connection
        }
    }
}
