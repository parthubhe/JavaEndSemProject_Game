package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.has.mt.*;
import com.has.mt.interfaces.GameExceptionMessages; // Import messages

import java.util.LinkedHashMap;
import java.util.Map;


public class CharacterSelectionScreen extends AbstractScreen {

    private Skin skin;
    private Preferences prefs;
    private String selectedCharacterType = null;
    private TextButton startButton;

    private final Map<String, CharacterPreviewData> characterPreviews = new LinkedHashMap<>();
    private Table previewTable;
    private Table mainTable;
    private Image selectedCharacterPreviewImage; // Static image for preview

    // Simple class to hold preview info
    private static class CharacterPreviewData {
        final String displayName;
        final String idleAssetPath;
        TextureRegion idleTextureRegion;
        // --- CHANGE START: Use TextButton specifically ---
        TextButton selectButton; // Changed from Button to TextButton
        // --- CHANGE END ---
        int frameWidth = 0;

        CharacterPreviewData(String displayName, String idleAssetPath) {
            this.displayName = displayName;
            this.idleAssetPath = idleAssetPath;
        }
    }


    private final String username;
    public CharacterSelectionScreen(MyGdxGame game, String username) {
        super(game);
        this.username = username;
        Gdx.app.log("CharacterSelectionScreen", "Screen created for user: " + username);
        prefs = Gdx.app.getPreferences(GameConfig.PREFS_NAME);

        // Define characters and their *IDLE* frame widths (cols)
        // VERIFY THESE FRAME WIDTHS (total width / number of columns)
        characterPreviews.put("Knight_1", new CharacterPreviewData("Knight", AssetLoader.KNIGHT_IDLE_PATH));
        characterPreviews.put("LightningMage", new CharacterPreviewData("Lightning Mage", AssetLoader.MAGE_IDLE_PATH));
        characterPreviews.put("FireWizard", new CharacterPreviewData("Fire Wizard", AssetLoader.FIRE_WIZARD_IDLE_PATH));
        characterPreviews.put("WandererMage", new CharacterPreviewData("Wanderer Mage", AssetLoader.WANDERER_MAGE_IDLE_PATH));
        characterPreviews.put("Samurai", new CharacterPreviewData("Samurai", AssetLoader.SAMURAI_IDLE_PATH));
        characterPreviews.put("SamuraiArcher", new CharacterPreviewData("Samurai Archer", AssetLoader.SAMURAI_ARCHER_IDLE_PATH));
        characterPreviews.put("SamuraiCommander", new CharacterPreviewData("Samurai Commander", AssetLoader.SAMURAI_COMMANDER_IDLE_PATH));

        loadPreviewTextures();
    }

    // Load static preview textures
    private void loadPreviewTextures() {
        Gdx.app.log("CharacterSelectionScreen", "Loading preview textures...");
        try {
            for (Map.Entry<String, CharacterPreviewData> entry : characterPreviews.entrySet()) {
                CharacterPreviewData data = entry.getValue();
                Texture idleTexture = game.assetLoader.get(data.idleAssetPath, Texture.class);

                int cols = 1; // Default to 1 column
                // Determine columns based on character type (VERIFY THESE)
                switch (entry.getKey()) {
                    case "Knight_1":        cols = 4; break;
                    case "LightningMage":   cols = 7; break;
                    case "FireWizard":      cols = 8; break;
                    case "WandererMage":    cols = 8; break;
                    case "Samurai":         cols = 6; break;
                    case "SamuraiArcher":   cols = 8; break;
                    case "SamuraiCommander":cols = 6; break;
                    default: Gdx.app.error("CharSelect", "Unknown character type for frame width: " + entry.getKey()); break;
                }
                data.frameWidth = idleTexture.getWidth() / cols;
                if(data.frameWidth <= 0) data.frameWidth = idleTexture.getWidth(); // Fallback

                data.idleTextureRegion = new TextureRegion(idleTexture, 0, 0, data.frameWidth, idleTexture.getHeight());
            }
            Gdx.app.log("CharacterSelectionScreen", "Preview textures prepared.");
        } catch (Exception e) {
            Gdx.app.error("CharacterSelectionScreen", "Failed to load/prepare preview textures", e);
            game.setScreen(new MainMenuScreen(game)); // Fallback
        }
    }


    @Override
    public void show() {
        super.show();
        Gdx.app.log("CharacterSelectionScreen", "Show called.");

        try {
            skin = game.assetLoader.get(AssetLoader.UI_SKIN_PATH, Skin.class);
        } catch (Exception e) {
            Gdx.app.error("CharacterSelectionScreen", GameExceptionMessages.SKIN_LOAD_FAILED, e);
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(GameConfig.UI_PADDING * 2);
        stage.addActor(mainTable);

        Label title = new Label("Select Your Character", skin, "title");
        mainTable.add(title).padBottom(GameConfig.UI_PADDING * 4).center().row();

        // Layout with preview box
        Table contentTable = new Table();

        // Left side: Scrollable list
        Table selectionListTable = new Table();
        selectionListTable.top().left();
        selectionListTable.defaults().pad(GameConfig.UI_PADDING / 2).width(250).left();

        for (Map.Entry<String, CharacterPreviewData> entry : characterPreviews.entrySet()) {
            final String characterType = entry.getKey();
            final CharacterPreviewData data = entry.getValue();

            // --- CHANGE START: Instantiate as TextButton ---
            data.selectButton = new TextButton(data.displayName, skin);
            // --- CHANGE END ---
            data.selectButton.getLabel().setAlignment(Align.left); // Align text inside button
            data.selectButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectCharacter(characterType, data.selectButton);
                }
            });
            selectionListTable.add(data.selectButton).row();
        }

        ScrollPane scrollPane = new ScrollPane(selectionListTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // Right side: Preview Area
        Table previewAreaTable = new Table();
        previewAreaTable.pad(GameConfig.UI_PADDING);
        previewAreaTable.top();

        Label previewLabel = new Label("Preview", skin);
        selectedCharacterPreviewImage = new Image();
        selectedCharacterPreviewImage.setAlign(Align.center);
        selectedCharacterPreviewImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        previewAreaTable.add(previewLabel).padBottom(GameConfig.UI_PADDING).center().row();
        previewAreaTable.add(selectedCharacterPreviewImage).size(300, 300).expand().center();


        // Add panes to content table
        contentTable.add(scrollPane).width(300).expandY().fillY().left().padRight(GameConfig.UI_PADDING * 3);
        contentTable.add(previewAreaTable).expand().fill();

        mainTable.add(contentTable).expand().fill().padBottom(GameConfig.UI_PADDING * 3).row();

        // Buttons
        startButton = new TextButton("Start Game", skin);
        startButton.setDisabled(true);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!startButton.isDisabled() && selectedCharacterType != null) {
                    startGame();
                }
            }
        });

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        Table bottomTable = new Table();
        bottomTable.add(backButton).width(200).pad(GameConfig.UI_PADDING);
        bottomTable.add(startButton).width(200).pad(GameConfig.UI_PADDING);

        mainTable.add(bottomTable).padTop(GameConfig.UI_PADDING * 2).center();
    }


    private void selectCharacter(String characterType, TextButton clickedButton) { // Changed param type
        Gdx.app.log("CharacterSelectionScreen", characterType + " selected by " + username);
        selectedCharacterType = characterType;
        startButton.setDisabled(false);

        // Update static preview image
        CharacterPreviewData selectedData = characterPreviews.get(characterType);
        if (selectedData != null && selectedData.idleTextureRegion != null) {
            selectedCharacterPreviewImage.setDrawable(new TextureRegionDrawable(selectedData.idleTextureRegion));
        } else {
            selectedCharacterPreviewImage.setDrawable(null);
            Gdx.app.error("CharSelect", "Preview texture region missing for: " + characterType);
        }

        // Visual feedback for buttons
        for (CharacterPreviewData data : characterPreviews.values()) {
            if (data.selectButton != null) {
                data.selectButton.setChecked(data.selectButton == clickedButton);
            }
        }
    }

    private void startGame() {
        Gdx.app.log("CharacterSelectionScreen", "Starting game with " + selectedCharacterType + " for user " + username);
        prefs.putString(GameConfig.PREF_KEY_SELECTED_CHAR, selectedCharacterType);
        prefs.putString(GameConfig.PREF_KEY_USERNAME, username);
        prefs.flush();

        try {
            Gdx.app.log("CharacterSelectionScreen", "Loading assets for " + selectedCharacterType + "...");
            game.assetLoader.loadPlayerAssets(selectedCharacterType);
            game.assetLoader.manager.finishLoading();
            Gdx.app.log("CharacterSelectionScreen", "Assets loaded. Switching to GameScreen.");

            game.setScreen(new GameScreen(game, selectedCharacterType, username));
            dispose();
        } catch (Exception e) {
            Gdx.app.error("CharacterSelectionScreen", "Failed to load assets or start game!", e);
            startButton.setDisabled(true);
            selectedCharacterType = null;
            selectedCharacterPreviewImage.setDrawable(null);
            for (CharacterPreviewData data : characterPreviews.values()) {
                if (data.selectButton != null) {
                    data.selectButton.setChecked(false);
                }
            }
            // Show an error dialog to the user
            Dialog errorDialog = new Dialog("Error", skin, "dialog") // Use "dialog" window style if available
                .text("Failed to load character assets.\n" + e.getMessage())
                .button("OK")
                .show(stage);
        }
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // No animation updates needed for static preview
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.log("CharacterSelectionScreen", "Disposing screen.");
    }
}
