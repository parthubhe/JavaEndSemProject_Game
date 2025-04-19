// src/com/has/mt/screens/CharacterSelectionScreen.java
package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences; // For saving selection
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.MyGdxGame;

public class CharacterSelectionScreen extends AbstractScreen {

    private Preferences prefs; // To store selection

    // **** CONSTRUCTOR TO MATCH THE CALL in MainMenuScreen.java ****
    public CharacterSelectionScreen(MyGdxGame game) {
        super(game);
        Gdx.app.log("CharacterSelectionScreen", "Screen created.");
        prefs = Gdx.app.getPreferences("GamePreferences"); // Get preferences file
    }

    @Override
    public void show() {
        super.show();
        Gdx.app.log("CharacterSelectionScreen", "Show called.");

        Skin skin = game.assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class);
        if (skin == null) {
            Gdx.app.error("CharacterSelectionScreen", "UI Skin not loaded!");
            skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // Fallback attempt
        }

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("Select Your Character", skin, "title");
        TextButton mageButton = new TextButton("Lightning Mage", skin);
        TextButton knightButton = new TextButton("Knight (NYI)", skin); // NYI = Not Yet Implemented
        TextButton backButton = new TextButton("Back", skin);

        mageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectCharacter("Mage");
            }
        });

        knightButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // selectCharacter("Knight"); // Enable when KnightPlayer is implemented
                Gdx.app.log("CharacterSelectionScreen", "Knight selected (NYI)");
                // Maybe show a message that it's not ready
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game)); // Go back to main menu
                dispose();
            }
        });


        table.add(title).colspan(2).padBottom(GameConfig.UI_PADDING * 4).row();
        table.add(mageButton).width(300).pad(GameConfig.UI_PADDING);
        table.add(knightButton).width(300).pad(GameConfig.UI_PADDING).row();
        table.add(backButton).colspan(2).width(200).padTop(GameConfig.UI_PADDING * 3);

        stage.addActor(table);

        // TODO: Add character preview images?
    }

    private void selectCharacter(String characterType) {
        Gdx.app.log("CharacterSelectionScreen", characterType + " selected!");
        prefs.putString("selectedCharacter", characterType); // Save selection
        prefs.flush(); // Ensure it's written to disk
        // Start the game with the selected character
        game.setScreen(new GameScreen(game, characterType));
        dispose(); // Dispose this screen
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.1f, 0.1f, 1); // Dark red background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.log("CharacterSelectionScreen", "Disposing screen.");
    }

    // Implement resize, pause, resume, hide if needed
}
