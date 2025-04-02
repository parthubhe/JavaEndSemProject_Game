package com.has.mt;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.has.mt.interfaces.GameExceptionMessages; // Import messages
import com.has.mt.screens.MainMenuScreen;

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

        try {
            // Start loading INITIAL assets immediately
            assetLoader.loadInitialAssets();
            // Block until initial loading is complete (UI skin, common assets)
            // Specific player assets are loaded later.
            Gdx.app.log("MyGdxGame", "Waiting for initial assets to load...");
            assetLoader.manager.finishLoading(); // Finish loading UI, enemies, previews etc.
            Gdx.app.log("MyGdxGame", "Initial assets loaded.");

            // Initialize DB AFTER assets potentially needed by DB error handling (like fonts/UI) are loaded
            dbManager = DatabaseManager.getInstance();

        } catch (GameLogicException e) {
            Gdx.app.error("MyGdxGame", "Critical error during initialization!", e);
            // Handle critical failure - maybe display an error message screen or exit
            // For now, just exit
            Gdx.app.exit();
            return; // Stop further execution
        } catch (Exception e) { // Catch any other unexpected exceptions
            Gdx.app.error("MyGdxGame", "Unexpected critical error during initialization!", e);
            Gdx.app.exit();
            return;
        }

        Gdx.app.log("MyGdxGame", "Initialization Complete. Starting Main Menu.");
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
            screen.dispose(); // Dispose the current screen first
        }
        // Dispose shared resources
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (assetLoader != null) assetLoader.dispose();
        if (dbManager != null) {
            dbManager.close(); // Close DB connection
        }
        Gdx.app.log("MyGdxGame", "Game Dispose Finished.");
    }
}
