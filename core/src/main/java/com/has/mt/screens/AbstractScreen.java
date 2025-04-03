package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.has.mt.GameConfig;
import com.has.mt.MyGdxGame;

public abstract class AbstractScreen implements Screen {
    protected final MyGdxGame game;
    protected final OrthographicCamera uiCamera;
    protected final Viewport uiViewport;
    protected final Stage stage;

    public AbstractScreen(final MyGdxGame game) {
        this.game = game;
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new FitViewport(GameConfig.V_WIDTH, GameConfig.V_HEIGHT, uiCamera);
        this.uiCamera.position.set(GameConfig.V_WIDTH / 2f, GameConfig.V_HEIGHT / 2f, 0);
        this.uiCamera.update();
        // Create the Stage AFTER checking if game.batch is valid
        if (game.batch == null) {
            Gdx.app.error(this.getClass().getSimpleName(), "Game SpriteBatch is null during screen creation!");
            // Handle this critical error, maybe throw?
            throw new IllegalStateException("Game SpriteBatch cannot be null for Stage creation.");
        }
        this.stage = new Stage(uiViewport, game.batch);
    }

    @Override
    public void show() {
        // Set input processor to the UI stage when screen is shown
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Concrete screens must clear the screen

        // Render UI Stage
        uiViewport.apply(); // Apply viewport BEFORE stage operations
        // --- CHANGE START: Catch potential Stage errors ---
        try {
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Use Gdx delta, cap frame time
            stage.draw();
        } catch (Exception e) {
            Gdx.app.error(this.getClass().getSimpleName(), "Error during Stage act/draw", e);
            // Depending on the error, might need to handle differently (e.g., skip draw)
        }
        // --- CHANGE END ---
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
