package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.has.mt.*;
import com.has.mt.model.PlayerStats;
import com.has.mt.screens.CharacterSelectionScreen;
import java.util.List;
import com.has.mt.interfaces.GameExceptionMessages;

public class MainMenuScreen extends AbstractScreen {

    private TextField usernameField;
    private Table leaderboardTable;
    private Skin skin;
    private Preferences prefs;

    public MainMenuScreen(final MyGdxGame game) {
        super(game);
        prefs = Gdx.app.getPreferences(GameConfig.PREFS_NAME);
    }

    @Override
    public void show() {
        super.show();
        Gdx.app.log("MainMenuScreen", "Showing Main Menu");

        try {
            // --- Robust Skin Loading ---
            if (game.assetLoader != null && game.assetLoader.manager != null && game.assetLoader.manager.isLoaded(AssetLoader.UI_SKIN_PATH)) {
                skin = game.assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class);
            } else {
                Gdx.app.error("MainMenuScreen", "Skin not pre-loaded by AssetLoader! Attempting fallback load.");
                // Ensure AssetLoader is used even for fallback if possible
                if(game.assetLoader != null) {
                    skin = game.assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class); // This might throw if not loaded
                } else {
                    skin = new Skin(Gdx.files.internal(AssetLoader.UI_SKIN_PATH)); // Direct load as last resort
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", GameExceptionMessages.SKIN_LOAD_FAILED, e);
            Gdx.app.exit(); // Exit if skin is absolutely necessary and failed loading
            return;
        }
        // Final check after attempts
        if (skin == null) {
            Gdx.app.error("MainMenuScreen", "Skin is NULL after loading attempts. Exiting.");
            Gdx.app.exit();
            return;
        }
        // --- End Robust Skin Loading ---


        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(GameConfig.UI_PADDING * 2);
        stage.addActor(mainTable); // Add main table to stage

        // Left side: Title, Username, Buttons
        Table leftTable = new Table();
        leftTable.top(); // Align content to the top

        Label titleLabel = new Label("Hack and Slash Game", skin, "title"); // Use "title" style if defined
        titleLabel.setAlignment(Align.center);
        leftTable.add(titleLabel).padBottom(GameConfig.UI_PADDING * 5).colspan(2).center().expandX().fillX().row();

        // Username Input
        Label userLabel = new Label("Username:", skin);
        usernameField = new TextField(prefs.getString(GameConfig.PREF_KEY_USERNAME, ""), skin);
        leftTable.add(userLabel).padRight(GameConfig.UI_PADDING);
        leftTable.add(usernameField).width(250).padBottom(GameConfig.UI_PADDING * 2).row();


        // Buttons
        TextButton charSelectButton = new TextButton("Select Character & Play", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        charSelectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    Gdx.app.log("MainMenuScreen", "Username is empty.");
                    // TODO: Add a visual cue (e.g., shake the text field, show a small label)
                    return;
                }
                Gdx.app.log("MainMenuScreen", "Character Select clicked for user: " + username);
                prefs.putString(GameConfig.PREF_KEY_USERNAME, username); // Save username
                prefs.flush();
                try {
                    game.setScreen(new CharacterSelectionScreen(game, username));
                    dispose();
                } catch (Throwable t) { // Catch broader errors
                    Gdx.app.error("MainMenuScreen", "Failed to create/set CharacterSelectionScreen", t);
                    // Optionally show an error dialog
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

        // Add buttons below username field
        leftTable.add(charSelectButton).width(300).pad(GameConfig.UI_PADDING).colspan(2).row();
        leftTable.add(exitButton).width(300).pad(GameConfig.UI_PADDING).colspan(2).row();

        // Right side: Leaderboard
        Table rightTable = new Table();
        rightTable.top(); // Align content to the top

        Label leaderboardTitle = new Label("Leaderboard (Top " + GameConfig.LEADERBOARD_SIZE + ")", skin);
        rightTable.add(leaderboardTitle).padBottom(GameConfig.UI_PADDING * 2).expandX().center().row();

        leaderboardTable = new Table(skin);
        leaderboardTable.pad(GameConfig.UI_PADDING);
        // Optional background/border can be added via skin:
        // leaderboardTable.setBackground("some-drawable-name");

        // Populate Leaderboard
        populateLeaderboard();

        ScrollPane scrollPane = new ScrollPane(leaderboardTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Only allow vertical scrolling

        rightTable.add(scrollPane).expand().fill().width(400); // Increase leaderboard width slightly


        // Add left and right tables to main table
        mainTable.add(leftTable).expandY().fillY().left().padRight(GameConfig.UI_PADDING * 5);
        mainTable.add(rightTable).expandY().fillY().right();

        // stage.setDebugAll(GameConfig.DEBUG_DRAW_BOXES); // Uncomment to debug layout
    }


    private void populateLeaderboard() {
        leaderboardTable.clearChildren(); // Clear previous entries
        leaderboardTable.defaults().pad(GameConfig.UI_PADDING / 2).align(Align.left); // Align text left

        // Header row
        leaderboardTable.add(new Label("Rank", skin, "small")).padRight(10).align(Align.center);
        leaderboardTable.add(new Label("Username", skin, "small")).expandX().fillX();
        leaderboardTable.add(new Label("Score", skin, "small")).minWidth(80).align(Align.right);
        leaderboardTable.row();
        // --- Simple spacing instead of Separator ---
        leaderboardTable.add().height(2).colspan(3).row();
        // --- End spacing ---


        try {
            if (game.dbManager == null) {
                throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "DatabaseManager is null in MainMenu");
            }
            List<PlayerStats> topPlayers = game.dbManager.getTopScores(GameConfig.LEADERBOARD_SIZE);

            if (topPlayers == null || topPlayers.isEmpty()) {
                leaderboardTable.add(new Label("No scores yet!", skin)).colspan(3).center().padTop(20).row();
            } else {
                int rank = 1;
                for (PlayerStats stats : topPlayers) {
                    if(stats == null) continue; // Skip null entries if DB query returned null somehow
                    leaderboardTable.add(new Label(rank + ".", skin)).align(Align.center).padRight(10);
                    leaderboardTable.add(new Label(stats.username, skin)).expandX().fillX(); // Allow username to take space
                    leaderboardTable.add(new Label(String.valueOf(stats.highestScore), skin)).align(Align.right);
                    leaderboardTable.row();
                    rank++;
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Failed to retrieve leaderboard data", e);
            leaderboardTable.add(new Label("Error loading scores.", skin)).colspan(3).center().padTop(20).row();
        }
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply(); // Apply viewport before acting/drawing
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.app.log("MainMenuScreen", "Hiding Main Menu");
    }

    @Override
    public void dispose() {
        super.dispose(); // Disposes the stage
        Gdx.app.log("MainMenuScreen", "Disposing Main Menu");
    }
}
