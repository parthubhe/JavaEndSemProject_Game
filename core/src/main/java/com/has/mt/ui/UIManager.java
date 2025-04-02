package com.has.mt.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*; // Import UI classes
import com.badlogic.gdx.utils.Align; // Import Align
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.GameLogicException; // Import exception
import com.has.mt.interfaces.GameExceptionMessages; // Import messages
import com.has.mt.gameobjects.Player;

public class UIManager implements Disposable {

    private Stage stage;
    private Skin skin;

    // HUD Elements
    private Table hudTable;
    private ProgressBar healthBar;
    private Label healthLabel;
    private Label scoreLabel;
    private Label usernameLabel;


    public UIManager(AssetLoader assetLoader, Stage stage) {
        if (stage == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "Stage in UIManager constructor");
        }
        if (assetLoader == null) {
            throw new GameLogicException(GameExceptionMessages.NULL_DEPENDENCY, "AssetLoader in UIManager constructor");
        }
        this.stage = stage;
        try {
            this.skin = assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class);
        } catch (Exception e) {
            Gdx.app.error("UIManager", GameExceptionMessages.SKIN_LOAD_FAILED, e);
            try { skin = new Skin(Gdx.files.internal(AssetLoader.UI_SKIN_PATH)); }
            catch (Exception fe) { throw new GameLogicException(GameExceptionMessages.SKIN_LOAD_FAILED, fe); }
        }
        if (this.skin == null) {
            throw new GameLogicException(GameExceptionMessages.SKIN_LOAD_FAILED + " (Result was null)");
        }
    }


    // Create HUD elements and add them to the stage
    public void createHUD(Player player, String username) {
        if (skin == null) {
            Gdx.app.error("UIManager", "Cannot create HUD, skin is null.");
            return;
        }
        if (player == null || player.healthComponent == null) {
            Gdx.app.error("UIManager", "Cannot create HUD, player or health component is null.");
            return;
        }
        Gdx.app.log("UIManager", "Creating HUD for user: " + username);

        hudTable = new Table();
        hudTable.top().left(); // Position HUD at top-left
        hudTable.setFillParent(true);
        hudTable.pad(GameConfig.UI_PADDING);

        // Username Label (Top Left)
        usernameLabel = new Label("Player: " + (username != null ? username : "Unknown"), skin);

        // Health Label & Bar
        healthLabel = new Label("HP:", skin);
        // Ensure progress bar style exists in skin
        try {
            // --- CHANGE START: Check if style exists before creating ---
            if (skin.has("default-horizontal", ProgressBar.ProgressBarStyle.class)) {
                healthBar = new ProgressBar(0f, 1f, 0.01f, false, skin); // Use default style name
                healthBar.setValue(player.healthComponent.getHealthPercentage());
                healthBar.setColor(Color.GREEN); // Initial color
            } else {
                Gdx.app.error("UIManager", "ProgressBar style 'default-horizontal' not found in skin!");
                healthBar = null; // Set to null if style missing
            }
            // --- CHANGE END ---
        } catch (Exception e) {
            Gdx.app.error("UIManager", "Failed to create ProgressBar", e);
            healthBar = null; // Set to null if creation failed
        }

        // Score Label (Top Right)
        scoreLabel = new Label("Score: 0", skin);


        // Layouting the HUD
        Table topLeftTable = new Table();
        topLeftTable.add(usernameLabel).left().row();
        topLeftTable.add(healthLabel).left().padRight(5);
        if (healthBar != null) { // Only add if created successfully
            topLeftTable.add(healthBar).width(250).height(25).left(); // Adjust size
        } else {
            topLeftTable.add(new Label(String.format("%d / %d", player.healthComponent.getCurrentHealth(), player.healthComponent.getMaxHealth()), skin)).left(); // Show text HP if bar fails
        }


        hudTable.add(topLeftTable).expandX().left(); // Push username/health to left
        hudTable.add(scoreLabel).expandX().right().padRight(GameConfig.UI_PADDING); // Push score to right with padding
        hudTable.row(); // End of top row


        stage.addActor(hudTable);
        Gdx.app.log("UIManager", "HUD created and added to stage.");
    }


    // Update HUD elements based on player state and score
    public void updateHUD(Player player, int score) {
        if (player == null || player.healthComponent == null) return; // Safety check

        // Update Health Bar (or label if bar failed)
        if (healthBar != null) {
            float healthPercent = player.healthComponent.getHealthPercentage();
            healthBar.setValue(healthPercent);

            // --- Color Change Logic ---
            if (healthPercent > 0.6f) healthBar.setColor(Color.GREEN);
            else if (healthPercent > 0.3f) healthBar.setColor(Color.ORANGE);
            else healthBar.setColor(Color.RED);
            // --- End Color Change ---

        } else if (healthLabel != null) {
            // Update text label if bar doesn't exist
            healthLabel.setText(String.format("HP: %d/%d", player.healthComponent.getCurrentHealth(), player.healthComponent.getMaxHealth()));
        }

        // Update Score Label
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log("UIManager", "Disposing HUD resources");
        if (hudTable != null) {
            hudTable.remove();
            hudTable = null;
        }
        healthBar = null;
        healthLabel = null;
        scoreLabel = null;
        usernameLabel = null;
    }
}
