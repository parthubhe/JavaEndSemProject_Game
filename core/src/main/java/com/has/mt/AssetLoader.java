// src/com/has/mt/AssetLoader.java
package com.has.mt;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
// import com.badlogic.gdx.graphics.g2d.TextureAtlas; // If using atlases
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException; // Import GdxRuntimeException

public class AssetLoader {
    public final AssetManager manager = new AssetManager();

    // Define asset paths
    public static final String UI_SKIN_PATH = "ui/uiskin.json";

    // --- Player: Lightning Mage ---
    public static final String MAGE_IDLE_PATH = "Characters/Mage/Lightning Mage/LM_Idle.png";
    public static final String MAGE_WALK_PATH = "Characters/Mage/Lightning Mage/LM_Walk.png";
    public static final String MAGE_JUMP_PATH = "Characters/Mage/Lightning Mage/LM_Jump.png";
    public static final String MAGE_RUN_PATH = "Characters/Mage/Lightning Mage/LM_Run.png";
    public static final String MAGE_LIGHT_ATTACK_PATH = "Characters/Mage/Lightning Mage/LM_LightAttack.png";
    public static final String MAGE_HEAVY_ATTACK_PATH = "Characters/Mage/Lightning Mage/LM_HeavyAttack.png";
    public static final String MAGE_CHARGED_PATH = "Characters/Mage/Lightning Mage/LM_Chargeball.png";
    public static final String MAGE_LIGHTNING_BALL_PATH = "Characters/Mage/Lightning Mage/LM_Charge.png"; // Projectile anim? Check this path
    public static final String MAGE_VADER_STRIKE_PATH = "Characters/Mage/Lightning Mage/LM_VaderStrike.png";
    public static final String MAGE_HURT_PATH = "Characters/Mage/Lightning Mage/LM_Hurt.png";
    public static final String MAGE_DEAD_PATH = "Characters/Mage/Lightning Mage/LM_Dead.png";

    // --- Player: Knight (Example) ---
    public static final String KNIGHT_IDLE_PATH = "Characters/Knight/Knight_1/Idle.png";
    public static final String KNIGHT_WALK_PATH = "Characters/Knight/Knight_1/Walk.png";

    // --- Enemy: Slime (Blue) ---
    public static final String SLIME_BLUE_IDLE_PATH = "Enemy/SlimeEnemy/Blue_Slime/Idle.png";
    public static final String SLIME_BLUE_WALK_PATH = "Enemy/SlimeEnemy/Blue_Slime/walk.png"; // Lowercase 'w' based on your asset tree
    public static final String SLIME_BLUE_RUN_PATH = "Enemy/SlimeEnemy/Blue_Slime/Run.png";
    public static final String SLIME_BLUE_ATTACK1_PATH = "Enemy/SlimeEnemy/Blue_Slime/Attack_1.png";
    public static final String SLIME_BLUE_ATTACK2_PATH = "Enemy/SlimeEnemy/Blue_Slime/Attack_2.png"; // Add if used
    public static final String SLIME_BLUE_ATTACK3_PATH = "Enemy/SlimeEnemy/Blue_Slime/Attack_3.png"; // Add if used
    public static final String SLIME_BLUE_HURT_PATH = "Enemy/SlimeEnemy/Blue_Slime/Hurt.png";
    public static final String SLIME_BLUE_DEAD_PATH = "Enemy/SlimeEnemy/Blue_Slime/Dead.png";
    public static final String SLIME_BLUE_JUMP_PATH = "Enemy/SlimeEnemy/Blue_Slime/Jump.png"; // Add if used

    // --- Enemy: Slime (Green) ---
    public static final String SLIME_GREEN_IDLE_PATH = "Enemy/SlimeEnemy/Green_Slime/Idle.png";
    public static final String SLIME_GREEN_WALK_PATH = "Enemy/SlimeEnemy/Green_Slime/Walk.png"; // Uppercase 'W' based on your asset tree
    public static final String SLIME_GREEN_RUN_PATH = "Enemy/SlimeEnemy/Green_Slime/Run.png";
    public static final String SLIME_GREEN_ATTACK1_PATH = "Enemy/SlimeEnemy/Green_Slime/Attack_1.png";
    public static final String SLIME_GREEN_ATTACK2_PATH = "Enemy/SlimeEnemy/Green_Slime/Attack_2.png"; // Add if used
    public static final String SLIME_GREEN_ATTACK3_PATH = "Enemy/SlimeEnemy/Green_Slime/Attack_3.png"; // Add if used
    public static final String SLIME_GREEN_HURT_PATH = "Enemy/SlimeEnemy/Green_Slime/Hurt.png";
    public static final String SLIME_GREEN_DEAD_PATH = "Enemy/SlimeEnemy/Green_Slime/Dead.png";
    public static final String SLIME_GREEN_JUMP_PATH = "Enemy/SlimeEnemy/Green_Slime/Jump.png"; // Add if used

    // --- Enemy: Slime (Red) - Add paths if needed ---
    // public static final String SLIME_RED_IDLE_PATH = "Enemy/SlimeEnemy/Red_Slime/Idle.png";
    // ... etc ...

    // --- Enemy: Skeleton (Example) ---
    public static final String SKELETON_IDLE_PATH = "Enemy/SkeletonEnemy/Skeleton_Warrior/Idle.png";
    public static final String SKELETON_WALK_PATH = "Enemy/SkeletonEnemy/Skeleton_Warrior/Walk.png";

    // --- Background Textures --- Dynamically loaded by ParallaxBackground is better

    // --- Floor Textures ---
    public static final String FLOOR_TEXTURE_SHEET = "FloorTextures/Textures-16.png";

    public void load() {
        Gdx.app.log("AssetLoader", "Starting asset loading...");

        // Load Skin
        manager.load(UI_SKIN_PATH, Skin.class);

        // Load Player Textures (Mage)
        manager.load(MAGE_IDLE_PATH, Texture.class);
        manager.load(MAGE_WALK_PATH, Texture.class);
        manager.load(MAGE_JUMP_PATH, Texture.class);
        manager.load(MAGE_RUN_PATH, Texture.class);
        manager.load(MAGE_LIGHT_ATTACK_PATH, Texture.class);
        manager.load(MAGE_HEAVY_ATTACK_PATH, Texture.class);
        manager.load(MAGE_CHARGED_PATH, Texture.class);
        manager.load(MAGE_LIGHTNING_BALL_PATH, Texture.class);
        manager.load(MAGE_VADER_STRIKE_PATH, Texture.class);
        manager.load(MAGE_HURT_PATH, Texture.class);
        manager.load(MAGE_DEAD_PATH, Texture.class);

        // Load Player Textures (Knight Example)
        manager.load(KNIGHT_IDLE_PATH, Texture.class);
        manager.load(KNIGHT_WALK_PATH, Texture.class);

        // --- FIX: Load ALL required Slime Textures ---
        // Blue Slime
        manager.load(SLIME_BLUE_IDLE_PATH, Texture.class);
        manager.load(SLIME_BLUE_WALK_PATH, Texture.class); // Added walk (lowercase 'w')
        manager.load(SLIME_BLUE_RUN_PATH, Texture.class);
        manager.load(SLIME_BLUE_ATTACK1_PATH, Texture.class);
        manager.load(SLIME_BLUE_ATTACK2_PATH, Texture.class); // Added attack2
        manager.load(SLIME_BLUE_ATTACK3_PATH, Texture.class); // Added attack3
        manager.load(SLIME_BLUE_HURT_PATH, Texture.class);
        manager.load(SLIME_BLUE_DEAD_PATH, Texture.class);
        manager.load(SLIME_BLUE_JUMP_PATH, Texture.class); // Added jump

        // Green Slime
        manager.load(SLIME_GREEN_IDLE_PATH, Texture.class);
        manager.load(SLIME_GREEN_WALK_PATH, Texture.class); // Added walk (uppercase 'W')
        manager.load(SLIME_GREEN_RUN_PATH, Texture.class);
        manager.load(SLIME_GREEN_ATTACK1_PATH, Texture.class); // Added attack1
        manager.load(SLIME_GREEN_ATTACK2_PATH, Texture.class); // Added attack2
        manager.load(SLIME_GREEN_ATTACK3_PATH, Texture.class); // Added attack3
        manager.load(SLIME_GREEN_HURT_PATH, Texture.class);    // Added hurt
        manager.load(SLIME_GREEN_DEAD_PATH, Texture.class);     // Added dead
        manager.load(SLIME_GREEN_JUMP_PATH, Texture.class); // Added jump
        // --------------------------------------------

        // Load Enemy Textures (Skeleton Example)
        manager.load(SKELETON_IDLE_PATH, Texture.class);
        manager.load(SKELETON_WALK_PATH, Texture.class);

        // Load Floor Textures
        manager.load(FLOOR_TEXTURE_SHEET, Texture.class);

        Gdx.app.log("AssetLoader", "Asset loading queued.");
        // manager.finishLoading(); // Uncomment this for synchronous loading (simpler for now)
    }

    // Improved get method with better error handling
    public <T> T get(String fileName, Class<T> type) {
        // Check if loading has finished (important if not using finishLoading() in create)
        // if (!manager.update()) {
        //     Gdx.app.log("AssetLoader", "Assets still loading... returning null for " + fileName);
        //     return null; // Or handle loading screen logic
        // }

        if (!manager.isLoaded(fileName, type)) {
            Gdx.app.error("AssetLoader", "Asset not loaded and requested via get(): " + fileName + " (Type: " + type.getSimpleName() + ")");
            // Attempt a synchronous load ONLY if absolutely necessary (can cause stutter)
            try {
                Gdx.app.log("AssetLoader", "Attempting synchronous load for: " + fileName);
                manager.load(fileName, type);
                manager.finishLoadingAsset(fileName); // Block until this one is loaded
                if (manager.isLoaded(fileName, type)) {
                    Gdx.app.log("AssetLoader", "Synchronous load successful for: " + fileName);
                    return manager.get(fileName, type);
                } else {
                    throw new GdxRuntimeException("Failed to synchronously load asset: " + fileName);
                }
            } catch (Exception e) {
                Gdx.app.error("AssetLoader", "Synchronous load FAILED for: " + fileName, e);
                // Return null or throw exception depending on how critical the asset is
                throw new GdxRuntimeException("Failed to load critical asset: " + fileName, e);
            }
        }
        // If already loaded
        return manager.get(fileName, type);
    }


    public void dispose() {
        Gdx.app.log("AssetLoader", "Disposing AssetManager.");
        manager.dispose();
    }
}
