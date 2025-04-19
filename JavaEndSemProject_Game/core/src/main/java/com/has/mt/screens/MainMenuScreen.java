// src/com/has/mt/screens/MainMenuScreen.java
package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.MyGdxGame;
// Import other screens (Make sure these classes exist!)
import com.has.mt.screens.GameScreen;
import com.has.mt.screens.CharacterSelectionScreen; // Assuming this exists

public class MainMenuScreen extends AbstractScreen {

    public MainMenuScreen(final MyGdxGame game) {
        super(game);
    }

    @Override
    public void show() {
        super.show(); // Sets input processor to the stage
        Gdx.app.log("MainMenuScreen", "Showing Main Menu");

        Skin skin = null;
        // Robust skin loading
        try {
            if (game.assetLoader.manager.isLoaded(AssetLoader.UI_SKIN_PATH)) {
                skin = game.assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class);
            } else {
                Gdx.app.error("MainMenuScreen", "Skin not loaded by AssetLoader. Path: " + AssetLoader.UI_SKIN_PATH);
                // Attempt fallback sync load or direct load IF NEEDED, but ideally AssetLoader handles it.
                try {
                    skin = new Skin(Gdx.files.internal(AssetLoader.UI_SKIN_PATH)); // Fallback direct load
                } catch (Exception fallbackEx) {
                    Gdx.app.error("MainMenuScreen", "Fallback skin load failed!", fallbackEx);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error accessing/getting skin", e);
        }

        if (skin == null) {
            Gdx.app.error("MainMenuScreen", "UI Skin is NULL. Cannot create UI.");
            // Optional: Display an error message on screen using a default font?
            // Or exit if UI is critical.
            Gdx.app.exit(); // Exit if no skin available
            return;
        }

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label("Hack and Slash Game", skin); // Use default style
        titleLabel.setAlignment(Align.center);

        TextButton startButton = new TextButton("Start Game", skin);
        TextButton charSelectButton = new TextButton("Select Character", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // Listeners with dispose()
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Start Game clicked");
                String selectedCharacter = "LightningMage"; // Placeholder
                try {
                    GameScreen gameScreen = new GameScreen(game, selectedCharacter);
                    game.setScreen(gameScreen);
                    dispose(); // Dispose this screen AFTER setting the new one
                } catch (Throwable t) { // Catch Throwable for more robust error logging
                    Gdx.app.error("MainMenuScreen", "Failed to create/set GameScreen", t);
                }
            }
        });

        charSelectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Character Select clicked");
                try {
                    CharacterSelectionScreen charScreen = new CharacterSelectionScreen(game);
                    game.setScreen(charScreen);
                    dispose(); // Dispose this screen AFTER setting the new one
                } catch (Throwable t) { // Catch Throwable
                    Gdx.app.error("MainMenuScreen", "Failed to create/set CharacterSelectionScreen", t);
                }
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Exit clicked");
                Gdx.app.exit();
            }
        });

        table.add(titleLabel).padBottom(GameConfig.UI_PADDING * 5).colspan(1).center().row();
        table.add(startButton).width(300).pad(GameConfig.UI_PADDING).row();
        table.add(charSelectButton).width(300).pad(GameConfig.UI_PADDING).row();
        table.add(exitButton).width(300).pad(GameConfig.UI_PADDING).row();

        stage.clear(); // Clear previous actors just in case show() is called multiple times
        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // 1. Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. Update and Draw the UI Stage
        // The stage uses the UI camera/viewport set in AbstractScreen
        stage.getViewport().apply(); // Apply the UI viewport
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        super.hide(); // Clears input processor
        Gdx.app.log("MainMenuScreen", "Hiding Main Menu");
    }

    @Override
    public void dispose() {
        super.dispose(); // Disposes the stage
        Gdx.app.log("MainMenuScreen", "Disposing Main Menu");
    }
}
