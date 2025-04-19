// src/com/has/mt/ui/UIManager.java
package com.has.mt.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.has.mt.AssetLoader;
import com.has.mt.GameConfig;
import com.has.mt.gameobjects.Player; // Needs Player class import

public class UIManager implements Disposable {

    private AssetLoader assetLoader;
    private Stage stage; // The UI stage passed from the screen
    private Skin skin;

    // HUD Elements
    private Table hudTable;
    private ProgressBar healthBar;
    private Label healthLabel;
    // Add other HUD elements like score, ammo, etc.
    // private Label scoreLabel;

    // --- FIX: Add constructor ---
    public UIManager(AssetLoader assetLoader, Stage stage) {
        this.assetLoader = assetLoader;
        this.stage = stage;
        try {
            // Ensure skin is loaded before creating UIManager
            if (!assetLoader.manager.isLoaded(AssetLoader.UI_SKIN_PATH)) {
                Gdx.app.error("UIManager", "Skin not loaded when UIManager created!");
                // Handle appropriately - maybe throw exception or load fallback
                this.skin = new Skin(Gdx.files.internal(AssetLoader.UI_SKIN_PATH)); // Risky fallback
            } else {
                this.skin = assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class);
            }
        } catch (Exception e) {
            Gdx.app.error("UIManager", "Error getting/loading skin in UIManager", e);
            // Consider exiting or using a very basic default if UI is critical
        }
        if (this.skin == null) {
            Gdx.app.error("UIManager", "Skin is NULL after constructor.");
        }
    }

    // --- FIX: Add createHUD method ---
    public void createHUD(Player player) {
        if (skin == null) {
            Gdx.app.error("UIManager", "Cannot create HUD, skin is null.");
            return;
        }
        Gdx.app.log("UIManager", "Creating HUD");

        hudTable = new Table();
        hudTable.top().left(); // Position HUD at top-left
        hudTable.setFillParent(true); // Make table fill the stage
        hudTable.pad(GameConfig.UI_PADDING); // Add padding

        // Health Label
        healthLabel = new Label("Health:", skin); // Use default label style
        // Health Bar (Requires ProgressBar style definition in your uiskin.json)
        // Example: ProgressBar.ProgressBarStyle: { default-horizontal: { background: ..., knobBefore: ... } }
        healthBar = new ProgressBar(0f, 1f, 0.01f, false, skin); // Min, Max, StepSize, Vertical, Skin
        healthBar.setValue(player.healthComponent.getHealthPercentage()); // Set initial value
        healthBar.setColor(Color.GREEN); // Or use style colors

        // Add elements to table
        hudTable.add(healthLabel).padRight(5);
        hudTable.add(healthBar).width(200).height(20).expandX().left(); // Expand bar horizontally
        hudTable.row(); // Move to next row for other elements

        // Add score label example
        // scoreLabel = new Label("Score: 0", skin);
        // hudTable.add(scoreLabel).colspan(2).left().padTop(10);
        // hudTable.row();

        // Add table to the stage
        stage.addActor(hudTable);
    }

    // --- FIX: Add updateHUD method ---
    public void updateHUD(Player player) {
        if (healthBar != null && player != null) {
            float healthPercent = player.healthComponent.getHealthPercentage();
            healthBar.setValue(healthPercent);

            // Change color based on health
            if (healthPercent > 0.6f) {
                healthBar.setColor(Color.GREEN);
            } else if (healthPercent > 0.3f) {
                healthBar.setColor(Color.ORANGE);
            } else {
                healthBar.setColor(Color.RED);
            }
        }
        // Update score label example
        // if (scoreLabel != null && player != null) {
        //    scoreLabel.setText("Score: " + player.getScore()); // Assuming player has getScore()
        // }
    }

    // --- FIX: Add dispose method ---
    @Override
    public void dispose() {
        Gdx.app.log("UIManager", "Disposing HUD resources (if any owned)");
        // Skin is managed by AssetLoader, Stage is managed by Screen.
        // If UIManager created any textures/fonts directly, dispose them here.
        // Removing actors from stage is handled when stage is disposed.
        if (hudTable != null) {
            hudTable.remove(); // Remove table from stage
        }
    }
}
