package com.has.mt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap; // For tracking loaded player assets
import com.badlogic.gdx.utils.Array; // For storing paths

import com.has.mt.interfaces.GameExceptionMessages; // Import exception messages
import java.util.HashSet; // Using HashSet for efficient checking
import java.util.Set;


public class AssetLoader {
    public final AssetManager manager = new AssetManager();

    // Keep track of which player's assets are currently loaded
    private String currentlyLoadedPlayerType = null;
    // Store the paths associated with each player type
    public final ObjectMap<String, Array<String>> playerAssetPaths = new ObjectMap<>(); // Made public for GameScreen check

    // Define asset paths
    public static final String UI_SKIN_PATH = "ui/uiskin.json";

    // --- Player Base Paths ---
    public static final String KNIGHT_BASE_PATH = "Characters/Knight/Knight_1/";
    public static final String MAGE_BASE_PATH = "Characters/Mage/Lightning Mage/";
    public static final String FIRE_WIZARD_BASE_PATH = "Characters/Mage/Fire vizard/";
    public static final String WANDERER_MAGE_BASE_PATH = "Characters/Mage/Wanderer Magican/"; // Corrected path name from tree
    public static final String SAMURAI_BASE_PATH = "Characters/Samurai/Samurai/";
    public static final String SAMURAI_ARCHER_BASE_PATH = "Characters/Samurai/Samurai_Archer/";
    public static final String SAMURAI_COMMANDER_BASE_PATH = "Characters/Samurai/Samurai_Commander/";

    // --- Define paths using base paths (Example for Knight & Mage) ---
    // Knight_1 (Frame counts VERIFIED based on previous attempt)
    public static final String KNIGHT_IDLE_PATH = KNIGHT_BASE_PATH + "Idle.png"; // 4x1
    public static final String KNIGHT_WALK_PATH = KNIGHT_BASE_PATH + "Walk.png"; // 8x1
    public static final String KNIGHT_RUN_PATH = KNIGHT_BASE_PATH + "Run.png"; // 7x1
    public static final String KNIGHT_JUMP_PATH = KNIGHT_BASE_PATH + "Jump.png"; // 6x1
    public static final String KNIGHT_ATTACK1_PATH = KNIGHT_BASE_PATH + "Attack 1.png"; // 5x1
    public static final String KNIGHT_ATTACK2_PATH = KNIGHT_BASE_PATH + "Attack 2.png"; // 4x1
    public static final String KNIGHT_ATTACK3_PATH = KNIGHT_BASE_PATH + "Attack 3.png"; // 4x1
    public static final String KNIGHT_HURT_PATH = KNIGHT_BASE_PATH + "Hurt.png"; // 2x1
    public static final String KNIGHT_DEAD_PATH = KNIGHT_BASE_PATH + "Dead.png"; // 6x1
    public static final String KNIGHT_DEFEND_PATH = KNIGHT_BASE_PATH + "Defend.png"; // 5x1

    // Lightning Mage (Frame counts from previous attempt)
    public static final String MAGE_IDLE_PATH = MAGE_BASE_PATH + "LM_Idle.png"; // 7x1
    public static final String MAGE_WALK_PATH = MAGE_BASE_PATH + "LM_Walk.png"; // 7x1
    public static final String MAGE_JUMP_PATH = MAGE_BASE_PATH + "LM_Jump.png"; // 8x1
    public static final String MAGE_RUN_PATH = MAGE_BASE_PATH + "LM_Run.png"; // 8x1
    public static final String MAGE_LIGHT_ATTACK_PATH = MAGE_BASE_PATH + "LM_LightAttack.png"; // 10x1
    public static final String MAGE_HEAVY_ATTACK_PATH = MAGE_BASE_PATH + "LM_HeavyAttack.png"; // 4x1
    public static final String MAGE_CHARGED_PATH = MAGE_BASE_PATH + "LM_Chargeball.png"; // 7x1
    public static final String MAGE_LIGHTNING_BALL_PATH = MAGE_BASE_PATH + "LM_Charge.png"; // 9x1 (Projectile anim)
    public static final String MAGE_VADER_STRIKE_PATH = MAGE_BASE_PATH + "LM_VaderStrike.png"; // 13x1
    public static final String MAGE_HURT_PATH = MAGE_BASE_PATH + "LM_Hurt.png"; // 3x1
    public static final String MAGE_DEAD_PATH = MAGE_BASE_PATH + "LM_Dead.png"; // 5x1

    // --- ADD PATHS FOR OTHER SELECTABLE CHARACTERS (Verify frame counts) ---
    // Fire Wizard
    public static final String FIRE_WIZARD_IDLE_PATH = FIRE_WIZARD_BASE_PATH + "Idle.png"; // 8x1?
    public static final String FIRE_WIZARD_WALK_PATH = FIRE_WIZARD_BASE_PATH + "Walk.png"; // 8x1?
    public static final String FIRE_WIZARD_RUN_PATH = FIRE_WIZARD_BASE_PATH + "Run.png"; // 8x1?
    public static final String FIRE_WIZARD_JUMP_PATH = FIRE_WIZARD_BASE_PATH + "Jump.png"; // 3x1?
    public static final String FIRE_WIZARD_ATTACK1_PATH = FIRE_WIZARD_BASE_PATH + "Attack_1.png"; // 8x1?
    public static final String FIRE_WIZARD_ATTACK2_PATH = FIRE_WIZARD_BASE_PATH + "Attack_2.png"; // 8x1?
    public static final String FIRE_WIZARD_HURT_PATH = FIRE_WIZARD_BASE_PATH + "Hurt.png"; // 3x1?
    public static final String FIRE_WIZARD_DEAD_PATH = FIRE_WIZARD_BASE_PATH + "Dead.png"; // 7x1?
    public static final String FIRE_WIZARD_CHARGE_PATH = FIRE_WIZARD_BASE_PATH + "Charge.png"; // ?x1
    public static final String FIRE_WIZARD_FIREBALL_PATH = FIRE_WIZARD_BASE_PATH + "Fireball.png"; // 5x1? (Projectile anim)
    public static final String FIRE_WIZARD_FLAME_JET_PATH = FIRE_WIZARD_BASE_PATH + "Flame_jet.png"; // 5x1? (Projectile anim)

    // Wanderer Mage
    public static final String WANDERER_MAGE_IDLE_PATH = WANDERER_MAGE_BASE_PATH + "Idle.png"; // 8x1?
    public static final String WANDERER_MAGE_WALK_PATH = WANDERER_MAGE_BASE_PATH + "Walk.png"; // 8x1?
    public static final String WANDERER_MAGE_RUN_PATH = WANDERER_MAGE_BASE_PATH + "Run.png"; // 8x1?
    public static final String WANDERER_MAGE_JUMP_PATH = WANDERER_MAGE_BASE_PATH + "Jump.png"; // 3x1?
    public static final String WANDERER_MAGE_ATTACK1_PATH = WANDERER_MAGE_BASE_PATH + "Attack_1.png"; // 8x1?
    public static final String WANDERER_MAGE_ATTACK2_PATH = WANDERER_MAGE_BASE_PATH + "Attack_2.png"; // 8x1?
    public static final String WANDERER_MAGE_HURT_PATH = WANDERER_MAGE_BASE_PATH + "Hurt.png"; // 3x1?
    public static final String WANDERER_MAGE_DEAD_PATH = WANDERER_MAGE_BASE_PATH + "Dead.png"; // 7x1?
    public static final String WANDERER_MAGE_CHARGE1_PATH = WANDERER_MAGE_BASE_PATH + "Charge_1.png"; // ?x1
    public static final String WANDERER_MAGE_CHARGE2_PATH = WANDERER_MAGE_BASE_PATH + "Charge_2.png"; // ?x1
    public static final String WANDERER_MAGE_MAGIC_ARROW_PATH = WANDERER_MAGE_BASE_PATH + "Magic_arrow.png"; // 6x1? (Projectile anim)
    public static final String WANDERER_MAGE_MAGIC_SPHERE_PATH = WANDERER_MAGE_BASE_PATH + "Magic_sphere.png"; // 8x1? (Projectile anim)

    // Samurai
    public static final String SAMURAI_IDLE_PATH = SAMURAI_BASE_PATH + "Idle.png"; // 6x1?
    public static final String SAMURAI_WALK_PATH = SAMURAI_BASE_PATH + "Walk.png"; // 8x1?
    public static final String SAMURAI_RUN_PATH = SAMURAI_BASE_PATH + "Run.png"; // 8x1?
    public static final String SAMURAI_JUMP_PATH = SAMURAI_BASE_PATH + "Jump.png"; // 3x1?
    public static final String SAMURAI_ATTACK1_PATH = SAMURAI_BASE_PATH + "Attack_1.png"; // 4x1?
    public static final String SAMURAI_ATTACK2_PATH = SAMURAI_BASE_PATH + "Attack_2.png"; // 4x1?
    public static final String SAMURAI_ATTACK3_PATH = SAMURAI_BASE_PATH + "Attack_3.png"; // 6x1?
    public static final String SAMURAI_HURT_PATH = SAMURAI_BASE_PATH + "Hurt.png"; // 3x1?
    public static final String SAMURAI_DEAD_PATH = SAMURAI_BASE_PATH + "Dead.png"; // 5x1?
    public static final String SAMURAI_PROTECTION_PATH = SAMURAI_BASE_PATH + "Protection.png"; // 1x1?

    // Samurai Archer
    public static final String SAMURAI_ARCHER_IDLE_PATH = SAMURAI_ARCHER_BASE_PATH + "Idle.png"; // 8x1?
    public static final String SAMURAI_ARCHER_WALK_PATH = SAMURAI_ARCHER_BASE_PATH + "Walk.png"; // 8x1?
    public static final String SAMURAI_ARCHER_RUN_PATH = SAMURAI_ARCHER_BASE_PATH + "Run.png"; // 8x1?
    public static final String SAMURAI_ARCHER_JUMP_PATH = SAMURAI_ARCHER_BASE_PATH + "Jump.png"; // 3x1?
    public static final String SAMURAI_ARCHER_ATTACK1_PATH = SAMURAI_ARCHER_BASE_PATH + "Attack_1.png"; // 6x1? Melee?
    public static final String SAMURAI_ARCHER_ATTACK2_PATH = SAMURAI_ARCHER_BASE_PATH + "Attack_2.png"; // 6x1? Melee?
    public static final String SAMURAI_ARCHER_ATTACK3_PATH = SAMURAI_ARCHER_BASE_PATH + "Attack_3.png"; // 6x1? Melee?
    public static final String SAMURAI_ARCHER_HURT_PATH = SAMURAI_ARCHER_BASE_PATH + "Hurt.png"; // 3x1?
    public static final String SAMURAI_ARCHER_DEAD_PATH = SAMURAI_ARCHER_BASE_PATH + "Dead.png"; // 8x1?
    public static final String SAMURAI_ARCHER_SHOT_PATH = SAMURAI_ARCHER_BASE_PATH + "Shot.png"; // 9x1? (Cast anim)
    public static final String SAMURAI_ARCHER_ARROW_PATH = SAMURAI_ARCHER_BASE_PATH + "Arrow.png"; // 6x1? (Projectile anim)

    // Samurai Commander
    public static final String SAMURAI_COMMANDER_IDLE_PATH = SAMURAI_COMMANDER_BASE_PATH + "Idle.png"; // 6x1?
    public static final String SAMURAI_COMMANDER_WALK_PATH = SAMURAI_COMMANDER_BASE_PATH + "Walk.png"; // 8x1?
    public static final String SAMURAI_COMMANDER_RUN_PATH = SAMURAI_COMMANDER_BASE_PATH + "Run.png"; // 8x1?
    public static final String SAMURAI_COMMANDER_JUMP_PATH = SAMURAI_COMMANDER_BASE_PATH + "Jump.png"; // 3x1?
    public static final String SAMURAI_COMMANDER_ATTACK1_PATH = SAMURAI_COMMANDER_BASE_PATH + "Attack_1.png"; // 4x1?
    public static final String SAMURAI_COMMANDER_ATTACK2_PATH = SAMURAI_COMMANDER_BASE_PATH + "Attack_2.png"; // 6x1?
    public static final String SAMURAI_COMMANDER_ATTACK3_PATH = SAMURAI_COMMANDER_BASE_PATH + "Attack_3.png"; // 8x1?
    public static final String SAMURAI_COMMANDER_HURT_PATH = SAMURAI_COMMANDER_BASE_PATH + "Hurt.png"; // 3x1?
    public static final String SAMURAI_COMMANDER_DEAD_PATH = SAMURAI_COMMANDER_BASE_PATH + "Dead.png"; // 5x1?
    public static final String SAMURAI_COMMANDER_PROTECT_PATH = SAMURAI_COMMANDER_BASE_PATH + "Protect.png"; // 1x1?

    // --- Enemy Paths --- (No changes needed here if correct before)
    // Slime Paths...
    public static final String SLIME_BLUE_IDLE_PATH = "Enemy/SlimeEnemy/Blue_Slime/Idle.png";
    public static final String SLIME_BLUE_WALK_PATH = "Enemy/SlimeEnemy/Blue_Slime/walk.png";
    public static final String SLIME_BLUE_RUN_PATH = "Enemy/SlimeEnemy/Blue_Slime/Run.png";
    public static final String SLIME_BLUE_ATTACK1_PATH = "Enemy/SlimeEnemy/Blue_Slime/Attack_1.png";
    public static final String SLIME_BLUE_ATTACK2_PATH = "Enemy/SlimeEnemy/Blue_Slime/Attack_2.png";
    public static final String SLIME_BLUE_ATTACK3_PATH = "Enemy/SlimeEnemy/Blue_Slime/Attack_3.png";
    public static final String SLIME_BLUE_HURT_PATH = "Enemy/SlimeEnemy/Blue_Slime/Hurt.png";
    public static final String SLIME_BLUE_DEAD_PATH = "Enemy/SlimeEnemy/Blue_Slime/Dead.png";
    public static final String SLIME_BLUE_JUMP_PATH = "Enemy/SlimeEnemy/Blue_Slime/Jump.png";
    public static final String SLIME_GREEN_IDLE_PATH = "Enemy/SlimeEnemy/Green_Slime/Idle.png";
    public static final String SLIME_GREEN_WALK_PATH = "Enemy/SlimeEnemy/Green_Slime/Walk.png";
    public static final String SLIME_GREEN_RUN_PATH = "Enemy/SlimeEnemy/Green_Slime/Run.png";
    public static final String SLIME_GREEN_ATTACK1_PATH = "Enemy/SlimeEnemy/Green_Slime/Attack_1.png";
    public static final String SLIME_GREEN_ATTACK2_PATH = "Enemy/SlimeEnemy/Green_Slime/Attack_2.png";
    public static final String SLIME_GREEN_ATTACK3_PATH = "Enemy/SlimeEnemy/Green_Slime/Attack_3.png";
    public static final String SLIME_GREEN_HURT_PATH = "Enemy/SlimeEnemy/Green_Slime/Hurt.png";
    public static final String SLIME_GREEN_DEAD_PATH = "Enemy/SlimeEnemy/Green_Slime/Dead.png";
    public static final String SLIME_GREEN_JUMP_PATH = "Enemy/SlimeEnemy/Green_Slime/Jump.png";
    public static final String SLIME_RED_IDLE_PATH = "Enemy/SlimeEnemy/Red_Slime/Idle.png";
    public static final String SLIME_RED_WALK_PATH = "Enemy/SlimeEnemy/Red_Slime/Walk.png";
    public static final String SLIME_RED_RUN_PATH = "Enemy/SlimeEnemy/Red_Slime/Run.png";
    public static final String SLIME_RED_ATTACK1_PATH = "Enemy/SlimeEnemy/Red_Slime/Attack_1.png";
    public static final String SLIME_RED_ATTACK2_PATH = "Enemy/SlimeEnemy/Red_Slime/Attack_2.png";
    public static final String SLIME_RED_ATTACK3_PATH = "Enemy/SlimeEnemy/Red_Slime/Attack_3.png";
    public static final String SLIME_RED_HURT_PATH = "Enemy/SlimeEnemy/Red_Slime/Hurt.png";
    public static final String SLIME_RED_DEAD_PATH = "Enemy/SlimeEnemy/Red_Slime/Dead.png";
    public static final String SLIME_RED_JUMP_PATH = "Enemy/SlimeEnemy/Red_Slime/Jump.png";
    // Skeleton Paths...
    public static final String SKELETON_WARRIOR_BASE_PATH = "Enemy/SkeletonEnemy/Skeleton_Warrior/";
    public static final String SKELETON_IDLE_PATH = SKELETON_WARRIOR_BASE_PATH + "Idle.png";
    public static final String SKELETON_WALK_PATH = SKELETON_WARRIOR_BASE_PATH + "Walk.png";
    public static final String SKELETON_RUN_PATH = SKELETON_WARRIOR_BASE_PATH + "Run.png";
    public static final String SKELETON_ATTACK1_PATH = SKELETON_WARRIOR_BASE_PATH + "Attack_1.png";
    public static final String SKELETON_ATTACK2_PATH = SKELETON_WARRIOR_BASE_PATH + "Attack_2.png";
    public static final String SKELETON_ATTACK3_PATH = SKELETON_WARRIOR_BASE_PATH + "Attack_3.png";
    public static final String SKELETON_HURT_PATH = SKELETON_WARRIOR_BASE_PATH + "Hurt.png";
    public static final String SKELETON_DEAD_PATH = SKELETON_WARRIOR_BASE_PATH + "Dead.png";
    public static final String SKELETON_PROTECT_PATH = SKELETON_WARRIOR_BASE_PATH + "Protect.png";
    // Minotaur Paths...
    public static final String MINOTAUR_1_BASE_PATH = "Enemy/Minotaur/Minotaur_1/";
    public static final String MINOTAUR_IDLE_PATH = MINOTAUR_1_BASE_PATH + "Idle.png";
    public static final String MINOTAUR_WALK_PATH = MINOTAUR_1_BASE_PATH + "Walk.png";
    public static final String MINOTAUR_ATTACK_PATH = MINOTAUR_1_BASE_PATH + "Attack.png";
    public static final String MINOTAUR_HURT_PATH = MINOTAUR_1_BASE_PATH + "Hurt.png";
    public static final String MINOTAUR_DEAD_PATH = MINOTAUR_1_BASE_PATH + "Dead.png";

    // --- Floor Textures ---
    public static final String FLOOR_TEXTURE_SHEET = "FloorTextures/Textures-16.png";

    public void loadInitialAssets() {
        Gdx.app.log("AssetLoader", "Starting INITIAL asset loading...");
        manager.load(UI_SKIN_PATH, Skin.class);
        manager.load(FLOOR_TEXTURE_SHEET, Texture.class);
        loadAllEnemyAssets();
        definePlayerAssetPaths();
        loadCharacterPreviewAssets();
        Gdx.app.log("AssetLoader", "Initial asset loading queued.");
    }

    // Defines which assets belong to which player type string
    private void definePlayerAssetPaths() {
        playerAssetPaths.clear();

        // Knight_1
        Array<String> knightPaths = new Array<>();
        knightPaths.add(KNIGHT_IDLE_PATH); knightPaths.add(KNIGHT_WALK_PATH); knightPaths.add(KNIGHT_RUN_PATH);
        knightPaths.add(KNIGHT_JUMP_PATH); knightPaths.add(KNIGHT_ATTACK1_PATH); knightPaths.add(KNIGHT_ATTACK2_PATH);
        knightPaths.add(KNIGHT_ATTACK3_PATH); knightPaths.add(KNIGHT_HURT_PATH); knightPaths.add(KNIGHT_DEAD_PATH);
        knightPaths.add(KNIGHT_DEFEND_PATH);
        playerAssetPaths.put("Knight_1", knightPaths);

        // LightningMage
        Array<String> magePaths = new Array<>();
        magePaths.add(MAGE_IDLE_PATH); magePaths.add(MAGE_WALK_PATH); magePaths.add(MAGE_JUMP_PATH);
        magePaths.add(MAGE_RUN_PATH); magePaths.add(MAGE_LIGHT_ATTACK_PATH); magePaths.add(MAGE_HEAVY_ATTACK_PATH);
        magePaths.add(MAGE_CHARGED_PATH); magePaths.add(MAGE_LIGHTNING_BALL_PATH); magePaths.add(MAGE_VADER_STRIKE_PATH);
        magePaths.add(MAGE_HURT_PATH); magePaths.add(MAGE_DEAD_PATH);
        playerAssetPaths.put("LightningMage", magePaths);

        // Fire Wizard
        Array<String> fireWizardPaths = new Array<>();
        fireWizardPaths.add(FIRE_WIZARD_IDLE_PATH); fireWizardPaths.add(FIRE_WIZARD_WALK_PATH); fireWizardPaths.add(FIRE_WIZARD_RUN_PATH);
        fireWizardPaths.add(FIRE_WIZARD_JUMP_PATH); fireWizardPaths.add(FIRE_WIZARD_ATTACK1_PATH); fireWizardPaths.add(FIRE_WIZARD_ATTACK2_PATH);
        fireWizardPaths.add(FIRE_WIZARD_HURT_PATH); fireWizardPaths.add(FIRE_WIZARD_DEAD_PATH); fireWizardPaths.add(FIRE_WIZARD_CHARGE_PATH);
        fireWizardPaths.add(FIRE_WIZARD_FIREBALL_PATH); fireWizardPaths.add(FIRE_WIZARD_FLAME_JET_PATH);
        playerAssetPaths.put("FireWizard", fireWizardPaths);

        // Wanderer Mage
        Array<String> wandererMagePaths = new Array<>();
        wandererMagePaths.add(WANDERER_MAGE_IDLE_PATH); wandererMagePaths.add(WANDERER_MAGE_WALK_PATH); wandererMagePaths.add(WANDERER_MAGE_RUN_PATH);
        wandererMagePaths.add(WANDERER_MAGE_JUMP_PATH); wandererMagePaths.add(WANDERER_MAGE_ATTACK1_PATH); wandererMagePaths.add(WANDERER_MAGE_ATTACK2_PATH);
        wandererMagePaths.add(WANDERER_MAGE_HURT_PATH); wandererMagePaths.add(WANDERER_MAGE_DEAD_PATH); wandererMagePaths.add(WANDERER_MAGE_CHARGE1_PATH);
        wandererMagePaths.add(WANDERER_MAGE_CHARGE2_PATH); wandererMagePaths.add(WANDERER_MAGE_MAGIC_ARROW_PATH); wandererMagePaths.add(WANDERER_MAGE_MAGIC_SPHERE_PATH);
        playerAssetPaths.put("WandererMage", wandererMagePaths);

        // Samurai
        Array<String> samuraiPaths = new Array<>();
        samuraiPaths.add(SAMURAI_IDLE_PATH); samuraiPaths.add(SAMURAI_WALK_PATH); samuraiPaths.add(SAMURAI_RUN_PATH);
        samuraiPaths.add(SAMURAI_JUMP_PATH); samuraiPaths.add(SAMURAI_ATTACK1_PATH); samuraiPaths.add(SAMURAI_ATTACK2_PATH);
        samuraiPaths.add(SAMURAI_ATTACK3_PATH); samuraiPaths.add(SAMURAI_HURT_PATH); samuraiPaths.add(SAMURAI_DEAD_PATH);
        samuraiPaths.add(SAMURAI_PROTECTION_PATH);
        playerAssetPaths.put("Samurai", samuraiPaths);

        // Samurai Archer
        Array<String> samuraiArcherPaths = new Array<>();
        samuraiArcherPaths.add(SAMURAI_ARCHER_IDLE_PATH); samuraiArcherPaths.add(SAMURAI_ARCHER_WALK_PATH); samuraiArcherPaths.add(SAMURAI_ARCHER_RUN_PATH);
        samuraiArcherPaths.add(SAMURAI_ARCHER_JUMP_PATH); samuraiArcherPaths.add(SAMURAI_ARCHER_ATTACK1_PATH); samuraiArcherPaths.add(SAMURAI_ARCHER_ATTACK2_PATH);
        samuraiArcherPaths.add(SAMURAI_ARCHER_ATTACK3_PATH); samuraiArcherPaths.add(SAMURAI_ARCHER_HURT_PATH); samuraiArcherPaths.add(SAMURAI_ARCHER_DEAD_PATH);
        samuraiArcherPaths.add(SAMURAI_ARCHER_SHOT_PATH); samuraiArcherPaths.add(SAMURAI_ARCHER_ARROW_PATH);
        playerAssetPaths.put("SamuraiArcher", samuraiArcherPaths);

        // Samurai Commander
        Array<String> samuraiCommanderPaths = new Array<>();
        samuraiCommanderPaths.add(SAMURAI_COMMANDER_IDLE_PATH); samuraiCommanderPaths.add(SAMURAI_COMMANDER_WALK_PATH); samuraiCommanderPaths.add(SAMURAI_COMMANDER_RUN_PATH);
        samuraiCommanderPaths.add(SAMURAI_COMMANDER_JUMP_PATH); samuraiCommanderPaths.add(SAMURAI_COMMANDER_ATTACK1_PATH); samuraiCommanderPaths.add(SAMURAI_COMMANDER_ATTACK2_PATH);
        samuraiCommanderPaths.add(SAMURAI_COMMANDER_ATTACK3_PATH); samuraiCommanderPaths.add(SAMURAI_COMMANDER_HURT_PATH); samuraiCommanderPaths.add(SAMURAI_COMMANDER_DEAD_PATH);
        samuraiCommanderPaths.add(SAMURAI_COMMANDER_PROTECT_PATH);
        playerAssetPaths.put("SamuraiCommander", samuraiCommanderPaths);

        Gdx.app.log("AssetLoader", "Defined asset paths for " + playerAssetPaths.size + " player types.");
    }

    // Loads only the IDLE textures needed for the character selection previews
    public void loadCharacterPreviewAssets() {
        Gdx.app.log("AssetLoader", "Loading Character Preview Assets (Idle)...");
        loadAsset(KNIGHT_IDLE_PATH, Texture.class);
        loadAsset(MAGE_IDLE_PATH, Texture.class);
        loadAsset(FIRE_WIZARD_IDLE_PATH, Texture.class);
        loadAsset(WANDERER_MAGE_IDLE_PATH, Texture.class);
        loadAsset(SAMURAI_IDLE_PATH, Texture.class);
        loadAsset(SAMURAI_ARCHER_IDLE_PATH, Texture.class);
        loadAsset(SAMURAI_COMMANDER_IDLE_PATH, Texture.class);
        Gdx.app.log("AssetLoader", "Character Preview Assets queued.");
    }

    // Loads ALL assets for a specific player type
    public void loadPlayerAssets(String playerType) {
        if (playerType == null || playerType.isEmpty()) {
            Gdx.app.log("AssetLoader", "Attempted to load assets for null or empty player type.");
            return;
        }
        if (playerType.equals(currentlyLoadedPlayerType)) {
            Gdx.app.debug("AssetLoader", "Player assets already loaded for: " + playerType);
            return;
        }

        unloadCurrentPlayerAssets();

        Gdx.app.log("AssetLoader", "Loading assets for player type: " + playerType);
        Array<String> pathsToLoad = playerAssetPaths.get(playerType);
        if (pathsToLoad != null && pathsToLoad.size > 0) {
            for (String path : pathsToLoad) {
                loadAsset(path, Texture.class);
            }
            currentlyLoadedPlayerType = playerType;
            Gdx.app.log("AssetLoader", "Assets queued for: " + playerType);
        } else {
            Gdx.app.error("AssetLoader", "No asset paths defined for player type: " + playerType);
            throw new GameLogicException(GameExceptionMessages.INVALID_PLAYER_TYPE, playerType);
        }
    }

    // Unloads assets for the currently loaded player type
    public void unloadCurrentPlayerAssets() {
        if (currentlyLoadedPlayerType != null) {
            Gdx.app.log("AssetLoader", "Unloading assets for player type: " + currentlyLoadedPlayerType);
            Array<String> pathsToUnload = playerAssetPaths.get(currentlyLoadedPlayerType);
            if (pathsToUnload != null) {
                int unloadedCount = 0;
                for (String path : pathsToUnload) {
                    if (manager.isLoaded(path)) {
                        manager.unload(path);
                        unloadedCount++;
                    }
                }
                Gdx.app.log("AssetLoader", "Unloaded " + unloadedCount + " assets for " + currentlyLoadedPlayerType);
            }
            currentlyLoadedPlayerType = null;
        }
    }

    // Loads all common enemy assets
    private void loadAllEnemyAssets() {
        Gdx.app.log("AssetLoader", "Loading All Enemy Assets...");
        // Slime...
        loadAsset(SLIME_BLUE_IDLE_PATH, Texture.class); loadAsset(SLIME_BLUE_WALK_PATH, Texture.class); loadAsset(SLIME_BLUE_RUN_PATH, Texture.class);
        loadAsset(SLIME_BLUE_ATTACK1_PATH, Texture.class); loadAsset(SLIME_BLUE_ATTACK2_PATH, Texture.class); loadAsset(SLIME_BLUE_ATTACK3_PATH, Texture.class);
        loadAsset(SLIME_BLUE_HURT_PATH, Texture.class); loadAsset(SLIME_BLUE_DEAD_PATH, Texture.class); loadAsset(SLIME_BLUE_JUMP_PATH, Texture.class);
        loadAsset(SLIME_GREEN_IDLE_PATH, Texture.class); loadAsset(SLIME_GREEN_WALK_PATH, Texture.class); loadAsset(SLIME_GREEN_RUN_PATH, Texture.class);
        loadAsset(SLIME_GREEN_ATTACK1_PATH, Texture.class); loadAsset(SLIME_GREEN_ATTACK2_PATH, Texture.class); loadAsset(SLIME_GREEN_ATTACK3_PATH, Texture.class);
        loadAsset(SLIME_GREEN_HURT_PATH, Texture.class); loadAsset(SLIME_GREEN_DEAD_PATH, Texture.class); loadAsset(SLIME_GREEN_JUMP_PATH, Texture.class);
        loadAsset(SLIME_RED_IDLE_PATH, Texture.class); loadAsset(SLIME_RED_WALK_PATH, Texture.class); loadAsset(SLIME_RED_RUN_PATH, Texture.class);
        loadAsset(SLIME_RED_ATTACK1_PATH, Texture.class); loadAsset(SLIME_RED_ATTACK2_PATH, Texture.class); loadAsset(SLIME_RED_ATTACK3_PATH, Texture.class);
        loadAsset(SLIME_RED_HURT_PATH, Texture.class); loadAsset(SLIME_RED_DEAD_PATH, Texture.class); loadAsset(SLIME_RED_JUMP_PATH, Texture.class);

        // Skeleton Warrior
        loadAsset(SKELETON_IDLE_PATH, Texture.class); loadAsset(SKELETON_WALK_PATH, Texture.class); loadAsset(SKELETON_RUN_PATH, Texture.class);
        loadAsset(SKELETON_ATTACK1_PATH, Texture.class); loadAsset(SKELETON_ATTACK2_PATH, Texture.class); loadAsset(SKELETON_ATTACK3_PATH, Texture.class);
        loadAsset(SKELETON_HURT_PATH, Texture.class); loadAsset(SKELETON_DEAD_PATH, Texture.class); loadAsset(SKELETON_PROTECT_PATH, Texture.class);

        // Minotaur 1
        loadAsset(MINOTAUR_IDLE_PATH, Texture.class); loadAsset(MINOTAUR_WALK_PATH, Texture.class); loadAsset(MINOTAUR_ATTACK_PATH, Texture.class);
        loadAsset(MINOTAUR_HURT_PATH, Texture.class); loadAsset(MINOTAUR_DEAD_PATH, Texture.class);

        Gdx.app.log("AssetLoader", "All Enemy Assets queued.");
    }

    // Helper method to safely queue loading and check file existence
    private <T> void loadAsset(String path, Class<T> type) {
        if (path == null || path.isEmpty()) {
            Gdx.app.error("AssetLoader", "Attempted to load asset with null or empty path.");
            return;
        }
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.error("AssetLoader", GameExceptionMessages.ASSET_NOT_FOUND + path);
            return; // Don't queue if file doesn't exist
        }
        if (!manager.isLoaded(path, type)) {
            manager.load(path, type);
        }
    }


    // Deprecated load() method
    @Deprecated
    public void load() {
        Gdx.app.log("AssetLoader", "Deprecated load() called. Use loadInitialAssets() instead.");
        loadInitialAssets();
    }


    // Improved get method
    public <T> T get(String fileName, Class<T> type) throws GameLogicException {
        if (fileName == null || fileName.isEmpty()) {
            throw new GameLogicException(GameExceptionMessages.ASSET_NOT_FOUND, "null or empty filename");
        }

        if (!manager.isLoaded(fileName, type)) {
            Gdx.app.error("AssetLoader", "Asset not loaded and requested via get(): " + fileName + " (Type: " + type.getSimpleName() + ")");
            if (!Gdx.files.internal(fileName).exists()) {
                // Throw specific exception if file missing during get()
                throw new GameLogicException(GameExceptionMessages.ASSET_NOT_FOUND + fileName);
            }
            // Attempt synchronous load
            try {
                Gdx.app.log("AssetLoader", "Attempting synchronous load for: " + fileName);
                manager.load(fileName, type);
                manager.finishLoadingAsset(fileName);
                if (manager.isLoaded(fileName, type)) {
                    Gdx.app.log("AssetLoader", "Synchronous load successful for: " + fileName);
                    return manager.get(fileName, type);
                } else {
                    throw new GameLogicException(GameExceptionMessages.ASSET_LOAD_FAILED, fileName + " (synchronous attempt failed)");
                }
            } catch (GdxRuntimeException e) { // Catch libGDX asset loading exceptions
                Gdx.app.error("AssetLoader", "Synchronous load FAILED (GdxRuntimeException): " + fileName, e);
                throw new GameLogicException(GameExceptionMessages.ASSET_LOAD_FAILED + fileName, e);
            } catch (Exception e) { // Catch any other unexpected exceptions
                Gdx.app.error("AssetLoader", "Synchronous load FAILED (Unknown Exception): " + fileName, e);
                throw new GameLogicException(GameExceptionMessages.ASSET_LOAD_FAILED + fileName + " - " + e.getMessage(), e);
            }
        }
        // If already loaded
        return manager.get(fileName, type);
    }

    public void dispose() {
        Gdx.app.log("AssetLoader", "Disposing AssetManager.");
        manager.dispose();
        currentlyLoadedPlayerType = null;
        playerAssetPaths.clear();
    }
}
