package com.has.mt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.has.mt.*;
import com.has.mt.managers.*;
import com.has.mt.gameobjects.Player;
import com.has.mt.gameobjects.Character;
import com.has.mt.gameobjects.players.*;
import com.has.mt.level.*;
import com.has.mt.ui.*;
import com.has.mt.utils.DebugUtils;
import com.badlogic.gdx.graphics.Color;
import com.has.mt.interfaces.GameExceptionMessages;


public class GameScreen extends AbstractScreen {

    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private LevelManager levelManager;
    private Player player;
    private EnemyManager enemyManager;
    private ProjectileManager projectileManager;
    private CollisionManager collisionManager;
    private UIManager uiManager;
    private final String selectedCharacterType;
    private final String username;
    private int score = 0;
    private int totalKillsThisGame = 0;
    private boolean gameIsOver = false;

    public GameScreen(final MyGdxGame game, String selectedCharacterType, String username) {
        super(game);
        this.selectedCharacterType = selectedCharacterType;
        this.username = username;
        Gdx.app.log("GameScreen", "Initializing for User: " + username + ", Character: " + selectedCharacterType);

        // Asset loading verification
        try {
            if (!checkPlayerAssetsLoaded(selectedCharacterType)) {
                Gdx.app.log("GameScreen", "Player assets not loaded before entering GameScreen. Attempting synchronous load for: " + selectedCharacterType);
                game.assetLoader.loadPlayerAssets(selectedCharacterType);
                game.assetLoader.manager.finishLoading();
                if (!checkPlayerAssetsLoaded(selectedCharacterType)){
                    throw new GameLogicException("Required player assets failed to load for: " + selectedCharacterType);
                }
                Gdx.app.log("GameScreen", "Synchronous player asset load successful.");
            }
        } catch (GameLogicException e) {
            Gdx.app.error("GameScreen", "Initialization failed! " + e.getMessage());
            game.setScreen(new MainMenuScreen(game)); // Go back to main menu
            dispose(); // Clean up partially initialized screen
            return;
        } catch (Exception e) { // Catch unexpected errors during asset loading/checking
            Gdx.app.error("GameScreen", "Unexpected error during asset check/load!", e);
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // Initialize Camera & Viewport
        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(GameConfig.V_WIDTH, GameConfig.V_HEIGHT, gameCamera);

        // Initialize Managers (order can matter)
        try {
            levelManager = new LevelManager(game.assetLoader);
            projectileManager = new ProjectileManager();
            enemyManager = new EnemyManager(game.assetLoader); // Pass DB manager if needed later
            uiManager = new UIManager(game.assetLoader, stage); // Stage is from AbstractScreen
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to initialize core managers!", e);
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // Initialize Player
        try {
            createPlayer(); // Creates and sets the 'player' instance
            if (player == null) {
                // createPlayer should throw if it fails, but double-check
                throw new GameLogicException("Player object is null after creation attempt for type: " + selectedCharacterType);
            }
        } catch(Exception e) {
            Gdx.app.error("GameScreen", "Player creation failed!", e);
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }

        // Initialize dependent managers
        try {
            collisionManager = new CollisionManager(player, enemyManager, projectileManager);
            enemyManager.setPlayerTarget(player); // Set target *after* player is created
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to initialize collision manager or set enemy target!", e);
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }


        // Load Level
        try {
            boolean levelLoaded = levelManager.loadLevel(0, player, enemyManager); // Load initial level
            if (!levelLoaded) {
                throw new GameLogicException(GameExceptionMessages.LEVEL_DATA_INVALID, "Failed to load initial level 0");
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load initial level!", e);
            game.setScreen(new MainMenuScreen(game));
            dispose();
            return;
        }


        // Setup HUD
        if (uiManager != null && player != null) {
            try {
                uiManager.createHUD(player, username);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Failed to create HUD!", e);
                // Continue game without HUD? Or go back? Decide based on importance.
            }
        } else {
            Gdx.app.error("GameScreen", "UIManager or Player is null, cannot create HUD.");
        }

        // Initial Camera Position
        if (player != null && player.position != null) {
            gameCamera.position.set(player.position.x, GameConfig.V_HEIGHT / 2f, 0);
            gameCamera.update();
        } else {
            Gdx.app.error("GameScreen", "Player or player position is null after init, cannot set camera.");
        }

        Gdx.app.log("GameScreen", "Initialization Complete for " + username);
    }

    private boolean checkPlayerAssetsLoaded(String playerType) {
        // Make sure playerAssetPaths itself isn't null
        if (game.assetLoader == null || game.assetLoader.playerAssetPaths == null) {
            Gdx.app.error("AssetCheck", "AssetLoader or playerAssetPaths map is null!");
            return false;
        }

        Array<String> paths = game.assetLoader.playerAssetPaths.get(playerType);
        if (paths == null || paths.isEmpty()) {
            Gdx.app.error("AssetCheck", "No paths defined for player type: " + playerType);
            return false;
        }
        // Determine idle path more robustly
        String idlePath = null;
        switch (playerType) {
            case "Knight_1": idlePath = AssetLoader.KNIGHT_IDLE_PATH; break;
            case "LightningMage": idlePath = AssetLoader.MAGE_IDLE_PATH; break;
            case "FireWizard": idlePath = AssetLoader.FIRE_WIZARD_IDLE_PATH; break;
            case "WandererMage": idlePath = AssetLoader.WANDERER_MAGE_IDLE_PATH; break;
            case "Samurai": idlePath = AssetLoader.SAMURAI_IDLE_PATH; break;
            case "SamuraiArcher": idlePath = AssetLoader.SAMURAI_ARCHER_IDLE_PATH; break;
            case "SamuraiCommander": idlePath = AssetLoader.SAMURAI_COMMANDER_IDLE_PATH; break;
            // Add other specific cases if needed
        }
        // If no specific case matched, try finding the one ending in "Idle.png"
        if (idlePath == null) {
            for(String path : paths) {
                if(path != null && path.endsWith("Idle.png")) {
                    idlePath = path;
                    break;
                }
            }
        }
        // Fallback to first path if no idle found
        if (idlePath == null) {
            idlePath = paths.first();
            Gdx.app.log("AssetCheck", "Could not determine specific idle path for " + playerType + ", checking first path: " + idlePath);
        }

        if (idlePath == null || !game.assetLoader.manager.isLoaded(idlePath, Texture.class)) {
            Gdx.app.error("AssetCheck", "Core asset not loaded for " + playerType + ": " + (idlePath != null ? idlePath : "path not found"));
            return false;
        }
        Gdx.app.debug("AssetCheck", "Core assets seem loaded for: " + playerType);
        return true;
    }

    private void createPlayer() {
        float startX = GameConfig.V_WIDTH / 4f;
        float startY = GameConfig.GROUND_Y;
        if (levelManager != null && levelManager.getCurrentLevel() != null) {
            LevelData data = levelManager.getCurrentLevel().getLevelData();
            if (data != null && data.playerStartPos != null) {
                startX = data.playerStartPos.x;
                startY = data.playerStartPos.y;
            }
        }
        Gdx.app.log("GameScreen", "Creating player of type: " + selectedCharacterType);
        // --- CHANGE START: Add cases for all new players ---
        switch (this.selectedCharacterType) {
            case "Knight_1":
                player = new KnightPlayer(game.assetLoader, startX, startY);
                break;
            case "LightningMage":
                player = new LightningMagePlayer(game.assetLoader, startX, startY, projectileManager);
                break;
            case "FireWizard":
                player = new FireWizardPlayer(game.assetLoader, startX, startY, projectileManager);
                break;
            case "WandererMage":
                player = new WandererMagePlayer(game.assetLoader, startX, startY, projectileManager);
                break;
            case "Samurai":
                player = new SamuraiPlayer(game.assetLoader, startX, startY); // No projectile manager needed?
                break;
            case "SamuraiArcher":
                player = new SamuraiArcherPlayer(game.assetLoader, startX, startY, projectileManager);
                break;
            case "SamuraiCommander":
                player = new SamuraiCommanderPlayer(game.assetLoader, startX, startY); // No projectile manager needed?
                break;
            // --- END CHANGE ---
            default:
                Gdx.app.error("GameScreen", "Unknown or unimplemented character type in createPlayer: " + selectedCharacterType + ". Defaulting to Mage.");
                player = new LightningMagePlayer(game.assetLoader, startX, startY, projectileManager); // Keep fallback
                break;
        }
        if (player != null) {
            player.reset(startX, startY);
        }
    }


    @Override
    public void show() {
        super.show();
        Gdx.app.log("GameScreen", "Showing Game Screen for " + username);
        gameIsOver = false;
        score = 0;
        totalKillsThisGame = 0;
        if (enemyManager != null) {
            enemyManager.resetKillCount();
            enemyManager.startSpawning(); // Ensure continuous spawning is active
        } else {
            Gdx.app.error("GameScreen", "EnemyManager is null in show()");
        }
    }

    private void update(float delta) {
        if (gameIsOver) return;

        if (InputManager.isActionJustPressed(InputManager.Action.PAUSE)) {
            Gdx.app.log("GameScreen", "Pause requested - NOT IMPLEMENTED");
            // game.setScreen(new PauseScreen(game, this)); // Future implementation
            return;
        }

        try {
            if (player != null) player.update(delta);
            if (enemyManager != null) {
                enemyManager.update(delta);
                int currentSessionKills = enemyManager.getKillCountThisSession(); // Get count without resetting yet
                if (currentSessionKills > totalKillsThisGame) {
                    int newKills = currentSessionKills - totalKillsThisGame;
                    score += newKills * GameConfig.ENEMY_KILL_SCORE;
                    totalKillsThisGame = currentSessionKills; // Update total tracked by GameScreen
                    Gdx.app.debug("GameScreen", "Score updated: " + score + " (Total Kills: " + totalKillsThisGame + ")");
                }
            }
            if (projectileManager != null) projectileManager.update(delta);
            if (collisionManager != null) collisionManager.checkCollisions();
            if (levelManager != null && player != null) levelManager.update(delta, player.position.x);

        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error during game update loop!", e);
            handleGameOver(false);
            gameIsOver = true;
            return;
        }

        // Update Camera (with safety checks)
        if (player != null && player.position != null && player.bounds != null && gameCamera != null && gameViewport != null) {
            float targetX = player.position.x + player.bounds.width / 2f;
            float lerpFactor = 0.1f;
            gameCamera.position.lerp(new Vector3(targetX, GameConfig.V_HEIGHT / 2f, 0), lerpFactor);
            float cameraHalfWidth = gameViewport.getWorldWidth() / 2f;
            float levelWidth = (levelManager != null && levelManager.getCurrentLevel() != null && levelManager.getCurrentLevel().getLevelData() != null)
                ? levelManager.getCurrentLevel().getLevelData().levelWidth
                : Float.MAX_VALUE;
            gameCamera.position.x = Math.max(cameraHalfWidth, gameCamera.position.x);
            if (levelWidth > gameViewport.getWorldWidth()) {
                gameCamera.position.x = Math.min(levelWidth - cameraHalfWidth, gameCamera.position.x);
            }
            gameCamera.update();
        }

        // Update HUD (with safety checks)
        if (uiManager != null && player != null) {
            uiManager.updateHUD(player, score);
        }

        // Check Game Over Conditions
        checkGameOverConditions();
    }

    private void checkGameOverConditions() {
        if (gameIsOver) return;
        boolean playerDead = (player == null || !player.isAlive());
        boolean playerWon = (score >= GameConfig.WIN_SCORE);

        if (playerDead) {
            boolean deathAnimFinished = (player != null && player.getCurrentState() == Character.State.DEAD && player.isAnimationFinished(Character.State.DEAD));
            if (player == null || deathAnimFinished) {
                Gdx.app.log("GameScreen", "Game Over: Player Died.");
                handleGameOver(false); // Player Lost
            }
        } else if (playerWon) {
            Gdx.app.log("GameScreen", "Game Over: Player Won!");
            handleGameOver(true); // Player Won
        }
    }

    private void handleGameOver(boolean playerWon) {
        if(gameIsOver) return; // Prevent multiple calls
        gameIsOver = true; // Set flag immediately
        Gdx.app.log("GameScreen", "Switching to GameOverScreen. Won: " + playerWon);
        if (enemyManager != null) {
            enemyManager.stopSpawning();
        }
        if (selectedCharacterType != null && game.assetLoader != null) { // Safety check asset loader
            game.assetLoader.unloadCurrentPlayerAssets();
        }
        // Ensure game instance is valid before setting screen
        if (game != null) {
            game.setScreen(new GameOverScreen(game, playerWon, score, totalKillsThisGame, username));
            dispose(); // Dispose this GameScreen after setting the new one
        } else {
            Gdx.app.error("GameScreen", "Game instance is null, cannot switch to GameOverScreen!");
        }
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update(delta);
        if (gameIsOver) {
            try { super.render(delta); }
            catch (Exception e) { Gdx.app.error("GameScreen", "Error rendering UI during game over transition", e); }
            return;
        }
        try {
            gameViewport.apply();
            game.batch.setProjectionMatrix(gameCamera.combined);
            game.batch.setColor(Color.WHITE);
            game.batch.begin();
            if (levelManager != null) {
                levelManager.renderBackground(game.batch, gameCamera.position.x);
                levelManager.renderFloor(game.batch, gameCamera.position.x);
            }
            game.batch.setColor(Color.WHITE); // Reset just in case
            if (enemyManager != null) enemyManager.render(game.batch);
            if (player != null) player.render(game.batch);
            if (projectileManager != null) projectileManager.render(game.batch);
            game.batch.end();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error during game world rendering!", e);
            if (game.batch != null && game.batch.isDrawing()) game.batch.end(); // Safely end batch
            handleGameOver(false); // Trigger game over on render error
        }
        if (GameConfig.DEBUG_DRAW_BOXES && !gameIsOver && game.shapeRenderer != null) { // Added shapeRenderer null check
            gameViewport.apply();
            game.shapeRenderer.setProjectionMatrix(gameCamera.combined);
            try {
                game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (player != null) player.drawDebug(game.shapeRenderer);
                if (enemyManager != null) enemyManager.drawDebug(game.shapeRenderer);
                game.shapeRenderer.end();
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Debug draw error", e);
                if (game.shapeRenderer.isDrawing()) game.shapeRenderer.end();
            }
        }
        try {
            super.render(delta); // Render UI Stage
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error during UI rendering!", e);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (gameViewport != null) gameViewport.update(width, height); // Added null check
        if (gameCamera != null) gameCamera.update(); // Added null check
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        Gdx.app.log("GameScreen", "Disposing Game Screen for user " + username);
        if (enemyManager != null) { enemyManager.stopSpawning(); } // Ensure spawning stops first
        if (player != null) { player.dispose(); player = null; }
        if (enemyManager != null) { enemyManager.dispose(); enemyManager = null; }
        if (projectileManager != null) { projectileManager.dispose(); projectileManager = null; }
        if (levelManager != null) { levelManager.dispose(); levelManager = null; }
        if (uiManager != null) { uiManager.dispose(); uiManager = null; }
        collisionManager = null;
        super.dispose(); // Disposes stage
        Gdx.app.log("GameScreen", "Game Screen dispose finished.");
    }

    @Override public void pause() { Gdx.app.log("GameScreen", "Pausing"); if (enemyManager != null) enemyManager.stopSpawning(); }
    @Override public void resume() { Gdx.app.log("GameScreen", "Resuming"); if (enemyManager != null && !gameIsOver) enemyManager.startSpawning(); } // Only restart if game not over
    @Override public void hide() { super.hide(); Gdx.app.log("GameScreen", "Hiding"); if (enemyManager != null) enemyManager.stopSpawning(); }

}
