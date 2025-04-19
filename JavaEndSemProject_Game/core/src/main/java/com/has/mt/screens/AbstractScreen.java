// src/com/has/mt/screens/AbstractScreen.java
package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
// import com.badlogic.gdx.graphics.GL20; // No longer needed here
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.has.mt.GameConfig;
import com.has.mt.MyGdxGame;

public abstract class AbstractScreen implements Screen {
    protected final MyGdxGame game;
    // Camera & Viewport specifically for the UI Stage
    protected final OrthographicCamera uiCamera;
    protected final Viewport uiViewport;
    protected final Stage stage; // The UI Stage

    public AbstractScreen(final MyGdxGame game) {
        this.game = game;
        this.uiCamera = new OrthographicCamera();
        // Use FitViewport for UI so it scales nicely
        this.uiViewport = new FitViewport(GameConfig.V_WIDTH, GameConfig.V_HEIGHT, uiCamera);
        // Center the UI camera
        this.uiCamera.position.set(GameConfig.V_WIDTH / 2f, GameConfig.V_HEIGHT / 2f, 0);
        this.uiCamera.update();
        // Create the Stage with the UI viewport and the shared game batch
        this.stage = new Stage(uiViewport, game.batch);
    }

    @Override
    public void show() {
        // Set input processor to the UI stage when screen is shown
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // --- NO SCREEN CLEARING HERE ---
        // Concrete screens MUST clear the screen themselves.

        // --- RENDER UI STAGE ONLY ---
        // Ensure the UI viewport is applied before acting/drawing the stage
        uiViewport.apply();
        // The stage manages its camera and batch projection internally when draw() is called.
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Update the UI viewport only. Game viewport is handled by GameScreen.
        uiViewport.update(width, height, true); // Center camera true
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        // Clear input processor if it's set to this stage
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log(this.getClass().getSimpleName(), "Disposing Stage.");
        if (stage != null) {
            stage.dispose();
        }
    }
}
