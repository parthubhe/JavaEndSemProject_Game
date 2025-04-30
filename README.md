# LibGDX 2D Hack and Slash Game [2D Platformer]

A 2D Hack and Slash game developed using Java and the LibGDX framework. Players select a character, fight waves of enemies, earn points, and try to reach a target score to win. The game features database integration for storing player stats and a leaderboard.
 
 
## Features

*   **Character Selection:** Choose from multiple character classes (Knight, Lightning Mage, Fire Wizard, Wanderer Mage, Samurai variants).
    *   Displays character name and an **animated idle preview** for visual feedback.
    *   Includes a "Start Game" button enabled only after selection.
*   **Dynamic Asset Loading:** Loads only the necessary sprite sheets for the *selected* player character, optimizing memory usage. Common assets and enemy assets are loaded upfront.
*   **Scoring System:** Earn points (`+100`) for each enemy defeated.
*   **Win Condition:** Reach a target score (default: 5000) to win the game.
*   **Game Over Screen:**
    *   Displays "VICTORY!" or "DEFEAT" based on the outcome.
    *   Shows final score and total kills.
    *   Provides "Play Again" (returns to Character Selection) and "Main Menu" buttons.
*   **Database Integration (MySQL):**
    *   **Username Input:** Players enter a username on the Main Menu.
    *   **Stats Tracking:** Stores username, total kills, highest score, last game outcome (Win/Loss), total wins, and total losses per user. Updates stats for existing usernames.
    *   **Leaderboard:** Displays the top 5 players ranked by their highest score on the Main Menu.
*   **Enemy Variety & Spawning:**
    *   Includes multiple enemy types: Slime (Blue, Green, Red), Skeleton Warrior, Minotaur.
    *   Enemies are spawned based on predefined points in `LevelData`.
    *   Basic Chase AI for enemies.
*   **Level System:**
    *   Loads levels defined in `LevelData` (currently hardcoded, expandable).
    *   Includes parallax scrolling backgrounds and tiled floors.
    *   Basic level transition logic based on player position.
*   **UI System:**
    *   Uses LibGDX Scene2D UI for menus and HUD.
    *   Game HUD displays Player Username, Health Bar, and current Score.
*   **Custom Exception Handling:** Implements a basic framework for custom game exceptions and error messages.
*   **Input Management:** Basic input handling for player movement, attacks, and pausing.
*   **Collision Detection:** Simple collision checks between player, enemies, and projectiles.

## Technologies Used

*   **Language:** Java (JDK 8 or higher recommended)
*   **Framework:** LibGDX (Cross-platform game development framework)
*   **Build Tool:** Gradle (Dependency management and build automation)
*   **Database:** MySQL
*   **UI Library:** LibGDX Scene2D UI

## Project Structure

The project follows a standard LibGDX Gradle structure. Key source directories within `core/src/main/java/com/has/mt/`:

*   `ai/`: Contains enemy AI logic (e.g., `BasicChaseAI`).
*   `components/`: Reusable game object components (e.g., `HealthComponent`, `AnimationComponent`).
*   `gameobjects/`: Base classes and specific implementations for game entities.
    *   `enemies/`: Concrete enemy classes (e.g., `SlimeEnemy`, `SkeletonEnemy`, `MinotaurEnemy`).
    *   `players/`: Concrete player classes (e.g., `LightningMagePlayer`, `KnightPlayer`).
*   `interfaces/`: Defines interfaces like `GameExceptionMessages`.
*   `level/`: Handles level loading, data representation, and background elements.
    *   `background/`: Parallax backgrounds and floor layers.
*   `managers/`: Core systems managing game state and objects (e.g., `GameManager`, `EnemyManager`, `AssetLoader`, `DatabaseManager`).
*   `model/`: Data model classes (e.g., `PlayerStats`).
*   `screens/`: Different game screens (e.g., `MainMenuScreen`, `GameScreen`, `CharacterSelectionScreen`, `GameOverScreen`).
*   `ui/`: User interface elements and management (e.g., `UIManager`, `GameHUD`, `AnimatedImage`).
*   `utils/`: Utility classes (e.g., `AnimationLoader`).

The `assets/` directory (typically located alongside `core`, or configured in Gradle) contains all game assets like images, fonts, and UI skins.

## Setup & Installation

1.  **Prerequisites:**
    *   Java Development Kit (JDK 8 or higher) installed.
    *   MySQL Server installed and running.
    *   Git installed.
    *   (Optional) An IDE like IntelliJ IDEA, Eclipse, or VS Code with Java/Gradle support.

2.  **Clone Repository:**
    ```bash
    git clone <your-repository-url>
    cd <repository-directory>
    ```

3.  **Database Setup:**
    *   Connect to your MySQL server.
    *   Create the database:
        ```sql
        CREATE DATABASE gamedb;
        ```
    *   **Crucially:** Configure the database connection details in `core/src/main/java/com/has/mt/DatabaseManager.java`. Update the following fields:
        *   `HOST`: Ensure this points to your MySQL server address (e.g., `jdbc:mysql://localhost:3306/`).
        *   `USER`: Your MySQL username (e.g., `root`).
        *   `PASS`: Your MySQL password.
        *   **Security Warning:** Do NOT commit your actual database password to the repository. Use environment variables, configuration files outside the repo, or other secure methods for production environments. For local development, ensure your `.gitignore` excludes any files containing credentials if you move them out.
    *   The necessary table (`player_stats`) will be created automatically by the `DatabaseManager` on the first run if it doesn't exist.

4.  **Import Project:**
    *   Open your IDE and import the project as a Gradle project. The IDE should automatically download dependencies defined in `build.gradle`.

5.  **Build Project (Optional - IDE usually handles this):**
    *   You can build the project using the Gradle wrapper:
        ```bash
        ./gradlew build
        ``` (Use `gradlew.bat build` on Windows)

## How to Run

*   **From IDE:**
    *   Find the `DesktopLauncher` class (usually in the `desktop` module).
    *   Right-click and select "Run" or "Debug".
*   **From Command Line:**
    *   Use the Gradle wrapper:
        ```bash
        ./gradlew desktop:run
        ``` (Use `gradlew.bat desktop:run` on Windows)

## Configuration

*   **Database:** Connection settings are in `core/src/main/java/com/has/mt/DatabaseManager.java` (see Setup section).
*   **Game Settings:** Various game parameters (scores, speeds, health, etc.) can be tweaked in `core/src/main/java/com/has/mt/GameConfig.java`.
*   **Assets:** Ensure all required assets (images, fonts, UI skin) are present in the correct structure within the `assets/` directory as expected by `AssetLoader.java`.
 

## Assets were fetched from craftpix.net, all the credits for the sprite sheets and backgrounds go to their original creators and this is only for non commmerical demonstration and use.
