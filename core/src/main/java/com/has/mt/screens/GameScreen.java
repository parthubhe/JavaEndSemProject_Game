// src/com/has/mt/screens/GameScreen.java
package com.has.mt.screens;

// --- FIX: Add necessary imports ---
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3; // Import Vector3 for lerp
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.has.mt.AssetLoader; // Import AssetLoader
import com.has.mt.GameConfig; // Import GameConfig
import com.has.mt.MyGdxGame; // Import MyGdxGame
import com.has.mt.managers.*; // Import all managers
import com.has.mt.gameobjects.Player; // Import Player
import com.has.mt.gameobjects.Character; // Import Character for State enum
import com.has.mt.gameobjects.players.*; // Import player implementations
import com.has.mt.level.*; // Import Level, LevelData, LevelManager
import com.has.mt.ui.*; // Import UIManager
import com.has.mt.utils.DebugUtils; // Import DebugUtils
import com.badlogic.gdx.graphics.Color;
// ---------------------------------

public class GameScreen extends AbstractScreen {

    // Game World Camera & Viewport
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;

    // Managers
    private LevelManager levelManager;
    private Player player; // Use the specific Player type after creation
    private EnemyManager enemyManager;
    private ProjectileManager projectileManager;
    private CollisionManager collisionManager;
    private UIManager uiManager;

    // State
    private String selectedCharacterType;

    public GameScreen(final MyGdxGame game, String selectedCharacterType) {
        super(game); // Initializes uiCamera, uiViewport, stage from AbstractScreen
        this.selectedCharacterType = selectedCharacterType; // Store selected type

        // 1. Init Game World Camera/Viewport
        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(GameConfig.V_WIDTH, GameConfig.V_HEIGHT, gameCamera);

        // 2. Init Managers (Order matters)
        // Ensure AssetLoader is passed correctly from 'game' instance
        levelManager = new LevelManager(game.assetLoader);
        projectileManager = new ProjectileManager();
        // Ensure DatabaseManager instance is accessible via 'game' or passed separately
        enemyManager = new EnemyManager(game.assetLoader, game.dbManager);
        // Ensure stage (from AbstractScreen) is passed correctly
        uiManager = new UIManager(game.assetLoader, stage);

        // 3. Init Player
        createPlayer(); // This sets the 'player' field
        if (player == null) {
            Gdx.app.error("GameScreen", "Player creation failed! Returning to Main Menu.");
            // Use the 'game' instance passed to the constructor to change screen
            game.setScreen(new MainMenuScreen(game));
            dispose(); // Clean up this screen's resources
            return;
        }

        // 4. Init remaining Managers that depend on Player
        // Ensure CollisionManager constructor is correct
        collisionManager = new CollisionManager(player, enemyManager, projectileManager);
        enemyManager.setPlayerTarget(player); // Set target for existing/future enemies

        // 5. Load Level (after Player exists so it can be positioned)
        levelManager.loadLevel(0, player, enemyManager);

        // 6. Setup HUD (after Player exists)
        uiManager.createHUD(player);

        // 7. Initial Camera Position (set after Player is positioned by level load)
        gameCamera.position.set(player.position.x, GameConfig.V_HEIGHT / 2f, 0);
        gameCamera.update();

        Gdx.app.log("GameScreen", "Initialization Complete");
    }

    private void createPlayer() {
        // Default start position
        float startX = GameConfig.V_WIDTH / 4f; // Use V_WIDTH from GameConfig
        float startY = GameConfig.GROUND_Y; // Use GROUND_Y from GameConfig

        // Try to get start pos from level if loaded (LevelData should be accessible)
        if (levelManager != null && levelManager.getCurrentLevel() != null) {
            LevelData data = levelManager.getCurrentLevel().getLevelData(); // Use getter
            if (data != null && data.playerStartPos != null) {
                startX = data.playerStartPos.x;
                startY = data.playerStartPos.y;
            }
        }

        Gdx.app.log("GameScreen", "Creating player of type: " + selectedCharacterType);
        switch (selectedCharacterType.toLowerCase()) {
            case "lightningmage":
                // Ensure LightningMagePlayer constructor matches
                player = new LightningMagePlayer(game.assetLoader, startX, startY, projectileManager);
                break;
            // case "knight": // Example for another character
            //    player = new KnightPlayer(game.assetLoader, startX, startY, ...); // Needs KnightPlayer class
            //    break;
            default:
                Gdx.app.error("GameScreen", "Unknown character type: " + selectedCharacterType + ". Defaulting to Mage.");
                player = new LightningMagePlayer(game.assetLoader, startX, startY, projectileManager);
                break;
        }

        if (player != null) {
            player.reset(startX, startY); // Ensure player starts correctly
        } else {
            Gdx.app.error("GameScreen", "Player object is null after creation attempt!");
        }
    }

    @Override
    public void show() {
        super.show(); // Sets InputProcessor to UI Stage
        Gdx.app.log("GameScreen", "Showing Game Screen");
    }

    private void update(float delta) {
        // Handle pause input first
        if (InputManager.isActionJustPressed(InputManager.Action.PAUSE)) {
            Gdx.app.log("GameScreen", "Pause requested (Implement PauseScreen)");
            // game.setScreen(new PauseScreen(game, this)); // TODO: Create PauseScreen
            return; // Skip game update if paused
        }

        // Update game logic
        if (player != null) player.update(delta);
        if (enemyManager != null) enemyManager.update(delta);
        if (projectileManager != null) projectileManager.update(delta);
        if (collisionManager != null) collisionManager.checkCollisions();
        if (levelManager != null && player != null) levelManager.update(delta, player.position.x);

        // Update Camera
        if (player != null) {
            float targetX = player.position.x + player.bounds.width / 2f; // Center on player approx
            // Smooth camera follow using interpolation (lerp)
            gameCamera.position.lerp(new Vector3(targetX, GameConfig.V_HEIGHT / 2f, 0), 0.1f); // Adjust lerp factor (0.1f) for desired smoothness
            // TODO: Add camera bounds clamping based on level width
            gameCamera.update();
        }

        // Update HUD
        if (uiManager != null && player != null) uiManager.updateHUD(player);

        // Check Game Over using fully qualified name for State enum
        if (player != null && !player.isAlive() && player.isAnimationFinished(com.has.mt.gameobjects.Character.State.DEAD)) {
            handleGameOver();
        }
    }

    private void handleGameOver() {
        Gdx.app.log("GameScreen", "Player is dead. Switching to Main Menu.");
        // TODO: Implement GameOverScreen later
        game.setScreen(new MainMenuScreen(game)); // Go back to menu
        dispose(); // Dispose this screen
    }

    @Override
    public void render(float delta) {
        // 1. Clear Screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. Update Game State
        update(delta);

        // 3. Render Game World
        gameViewport.apply();
        game.batch.setProjectionMatrix(gameCamera.combined);

        // --- Ensure batch color is white before drawing world ---
        game.batch.setColor(Color.WHITE); // Explicitly set to white
        // ------------------------------------------------------
        game.batch.begin();
        if (levelManager != null) {
            levelManager.renderBackground(game.batch, gameCamera.position.x); // Background now handles its own color/reset
            levelManager.renderFloor(game.batch, gameCamera.position.x);       // Floor now handles its own color/reset
        }
        // --- Ensure batch color is white before drawing characters ---
        game.batch.setColor(Color.WHITE); // Belt-and-suspenders approach
        // ---------------------------------------------------------
        if (enemyManager != null) enemyManager.render(game.batch);
        if (player != null) player.render(game.batch);
        if (projectileManager != null) projectileManager.render(game.batch);
        game.batch.end();


        // 4. Render Debug (Optional)
        if (GameConfig.DEBUG_DRAW_BOXES) {
            gameViewport.apply();
            game.shapeRenderer.setProjectionMatrix(gameCamera.combined);
            // ShapeRenderer uses its own color methods, no batch conflict here
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            try {
                if (player != null) DebugUtils.drawDebugLines(game.shapeRenderer, player);
                if (enemyManager != null) enemyManager.drawDebug(game.shapeRenderer);
            } catch (Exception e) { Gdx.app.error("GameScreen", "Debug draw error", e); }
            finally { if (game.shapeRenderer.isDrawing()) game.shapeRenderer.end(); }
        }

        // 5. Render UI
        super.render(delta); // Draws the UI Stage
    }
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height); // Update game viewport
        gameCamera.update(); // Update game camera associated with game viewport
        super.resize(width, height); // Update UI viewport via AbstractScreen
    }

    @Override
    public void dispose() {
        Gdx.app.log("GameScreen", "Disposing Game Screen");
        // Dispose in reverse order of creation or based on dependencies
        if (player != null) player.dispose();
        if (enemyManager != null) enemyManager.dispose();
        if (projectileManager != null) projectileManager.dispose();
        if (levelManager != null) levelManager.dispose();
        if (uiManager != null) uiManager.dispose();
        // CollisionManager typically doesn't need dispose unless it holds Disposable resources
        super.dispose(); // Disposes UI stage
    }

    // --- Other lifecycle methods ---
    @Override public void pause() { Gdx.app.log("GameScreen", "Pausing"); }
    @Override public void resume() { Gdx.app.log("GameScreen", "Resuming"); }
    @Override public void hide() { super.hide(); Gdx.app.log("GameScreen", "Hiding"); }

} // End of class
