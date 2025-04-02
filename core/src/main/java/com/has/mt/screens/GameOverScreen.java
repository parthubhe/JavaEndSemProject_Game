package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.has.mt.GameConfig;
import com.has.mt.MyGdxGame;
import com.has.mt.AssetLoader;

public class GameOverScreen extends AbstractScreen {

    private final boolean playerWon;
    private final int finalScore;
    private final int kills;
    private final String username;

    public GameOverScreen(MyGdxGame game, boolean playerWon, int finalScore, int kills, String username) {
        super(game);
        this.playerWon = playerWon;
        this.finalScore = finalScore;
        this.kills = kills;
        this.username = username;
        saveGameResult();
    }

    private void saveGameResult() {
        if (username == null || username.trim().isEmpty()) {
            Gdx.app.log("GameOverScreen", "Cannot save result, username is empty.");
            return;
        }
        try {
            // --- Ensure DB Manager instance exists ---
            if (game.dbManager == null) {
                Gdx.app.error("GameOverScreen", "DatabaseManager is null, cannot save score!");
                return;
            }
            // --- End Check ---

            String outcome = playerWon ? "WIN" : "LOSE";
            Gdx.app.log("GameOverScreen", "Saving game result for " + username + ": Score=" + finalScore + ", Kills=" + kills + ", Outcome=" + outcome);
            game.dbManager.addOrUpdatePlayerStats(username, kills, finalScore, outcome);
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Failed to save game result to database for user: " + username, e);
        }
    }


    @Override
    public void show() {
        super.show();
        Gdx.app.log("GameOverScreen", "Showing Game Over Screen. Player Won: " + playerWon);

        Skin skin;
        try {
            // --- Ensure AssetLoader exists ---
            if (game.assetLoader == null) throw new IllegalStateException("AssetLoader is null in GameOverScreen");
            // --- End Check ---
            skin = game.assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class);
        } catch (Exception e){
            Gdx.app.error("GameOverScreen", "Failed to load skin", e);
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label(playerWon ? "VICTORY!" : "DEFEAT", skin, "title");
        titleLabel.setColor(playerWon ? Color.GREEN : Color.RED);
        titleLabel.setAlignment(Align.center);

        Label scoreLabel = new Label("Final Score: " + finalScore, skin);
        scoreLabel.setAlignment(Align.center);
        Label killsLabel = new Label("Kills: " + kills, skin);
        killsLabel.setAlignment(Align.center);

        TextButton restartButton = new TextButton("Play Again", skin);
        TextButton exitButton = new TextButton("Main Menu", skin);

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("GameOverScreen", "Restart clicked");
                Preferences prefs = Gdx.app.getPreferences(GameConfig.PREFS_NAME);
                String lastUser = prefs.getString(GameConfig.PREF_KEY_USERNAME, "");
                game.setScreen(new CharacterSelectionScreen(game, lastUser));
                dispose();
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("GameOverScreen", "Exit to Main Menu clicked");
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        table.add(titleLabel).padBottom(GameConfig.UI_PADDING * 4).colspan(2).row();
        table.add(scoreLabel).padBottom(GameConfig.UI_PADDING).colspan(2).row();
        table.add(killsLabel).padBottom(GameConfig.UI_PADDING * 3).colspan(2).row();
        table.add(restartButton).width(250).pad(GameConfig.UI_PADDING);
        table.add(exitButton).width(250).pad(GameConfig.UI_PADDING).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.log("GameOverScreen", "Disposing Game Over Screen");
    }
}
