package com.has.mt;

import java.sql.*;
import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List
import java.util.Properties;
import com.badlogic.gdx.Gdx; // For logging
import com.has.mt.interfaces.GameExceptionMessages; // Import messages
import com.has.mt.model.PlayerStats; // Import the PlayerStats model


public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private final String HOST = "jdbc:mysql://localhost:3306/";
    private final String DB_NAME = "gamedb";
    private final String DB_URL = HOST + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String USER = "root";
    private final String PASS = "P@rthubh3"; // Your password

    // Private constructor for singleton
    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASS);

            try (Connection tempConnection = DriverManager.getConnection(HOST + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", props)) {
                try (Statement stmt = tempConnection.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                    Gdx.app.log("DatabaseManager", "Database '" + DB_NAME + "' checked/created.");
                }
            } catch (SQLException e) {
                Gdx.app.error("DatabaseManager", "Failed to check/create database.", e);
                throw new GameLogicException(GameExceptionMessages.DATABASE_CONNECTION_FAILED, "Could not create DB", e);
            }

            connection = DriverManager.getConnection(DB_URL, props);
            Gdx.app.log("DatabaseManager", "Database connection successful.");
            initializeDatabase();

        } catch (ClassNotFoundException e) {
            Gdx.app.error("DatabaseManager", "MySQL JDBC Driver not found.", e);
            throw new GameLogicException("MySQL JDBC Driver not found.", e);
        } catch (SQLException e) {
            Gdx.app.error("DatabaseManager", "Database Connection Failed!", e);
            connection = null;
            throw new GameLogicException(GameExceptionMessages.DATABASE_CONNECTION_FAILED, e.getMessage(), e);
        } catch (Exception e) {
            Gdx.app.error("DatabaseManager", "Initialization failed!", e);
            connection = null;
            throw new GameLogicException("Database Manager initialization failed.", e);
        }
    }

    // Singleton instance getter
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            Gdx.app.error("DatabaseManager", "Error checking DB connection status", e);
            return false;
        }
    }

    // Create/update the player_stats table
    public void initializeDatabase() {
        if (!isConnected()) {
            Gdx.app.error("DatabaseManager", "Not connected, cannot initialize table.");
            return;
        }
        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_stats ("
            + "username VARCHAR(100) PRIMARY KEY,"
            + "killCount INT DEFAULT 0,"
            + "highestScore INT DEFAULT 0,"
            + "lastOutcome VARCHAR(10) DEFAULT 'NONE',"
            + "totalWins INT DEFAULT 0,"
            + "totalLosses INT DEFAULT 0"
            + ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            Gdx.app.log("DatabaseManager", "'player_stats' table checked/created.");
        } catch (SQLException e) {
            Gdx.app.error("DatabaseManager", GameExceptionMessages.DATABASE_UPDATE_FAILED + " table init", e); // Added context
        }
    }


    /**
     * Adds a new player or updates existing player's stats after a game.
     * Increments kill count, updates highest score if the new score is better,
     * sets the last outcome, and increments win/loss counters.
     */
    public void addOrUpdatePlayerStats(String username, int killsThisGame, int scoreThisGame, String outcome) {
        if (!isConnected()) {
            Gdx.app.error("DatabaseManager", "Not connected, cannot update player stats for " + username);
            return;
        }
        if (username == null || username.trim().isEmpty()) {
            Gdx.app.error("DatabaseManager", "Cannot update stats for null or empty username.");
            return;
        }
        if (!"WIN".equalsIgnoreCase(outcome) && !"LOSE".equalsIgnoreCase(outcome)) {
            Gdx.app.error("DatabaseManager", "Invalid outcome provided: " + outcome);
            return;
        }

        String upsertSQL = "INSERT INTO player_stats (username, killCount, highestScore, lastOutcome, totalWins, totalLosses) "
            + "VALUES (?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "killCount = killCount + VALUES(killCount), "
            + "highestScore = GREATEST(highestScore, VALUES(highestScore)), "
            + "lastOutcome = VALUES(lastOutcome), "
            + "totalWins = totalWins + VALUES(totalWins), "
            + "totalLosses = totalLosses + VALUES(totalLosses);";

        try (PreparedStatement pstmt = connection.prepareStatement(upsertSQL)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, killsThisGame);
            pstmt.setInt(3, scoreThisGame);
            pstmt.setString(4, outcome.toUpperCase());
            pstmt.setInt(5, "WIN".equalsIgnoreCase(outcome) ? 1 : 0);
            pstmt.setInt(6, "LOSE".equalsIgnoreCase(outcome) ? 1 : 0);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                Gdx.app.log("DatabaseManager", "Player stats updated successfully for: " + username);
            } else {
                Gdx.app.log("DatabaseManager", "Player stats upsert affected 0 rows for: " + username); // Could happen if initial values are 0
            }

        } catch (SQLException e) {
            Gdx.app.error("DatabaseManager", GameExceptionMessages.DATABASE_UPDATE_FAILED + " for user " + username, e);
        }
    }


    /**
     * Retrieves the top N players based on their highest score.
     */
    public List<PlayerStats> getTopScores(int limit) {
        if (!isConnected()) {
            Gdx.app.error("DatabaseManager", "Not connected, cannot retrieve top scores.");
            return new ArrayList<>();
        }
        List<PlayerStats> topPlayers = new ArrayList<>();
        String querySQL = "SELECT username, killCount, highestScore, lastOutcome, totalWins, totalLosses "
            + "FROM player_stats ORDER BY highestScore DESC LIMIT ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PlayerStats stats = new PlayerStats(
                    rs.getString("username"),
                    rs.getInt("killCount"),
                    rs.getInt("highestScore"),
                    rs.getString("lastOutcome"),
                    rs.getInt("totalWins"),
                    rs.getInt("totalLosses")
                );
                topPlayers.add(stats);
            }
        } catch (SQLException e) {
            Gdx.app.error("DatabaseManager", GameExceptionMessages.DATABASE_QUERY_FAILED + " (getTopScores)", e);
        }
        return topPlayers;
    }

    // Example: Get specific player's total kills
    public int getTotalKillCount(String username) {
        if (!isConnected()) {
            Gdx.app.error("DatabaseManager", "Not connected, cannot get kill count for " + username);
            return 0;
        }
        if (username == null || username.trim().isEmpty()) return 0; // Handle empty username

        String querySQL = "SELECT killCount FROM player_stats WHERE username = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("killCount");
            }
        } catch (SQLException e) {
            Gdx.app.error("DatabaseManager", GameExceptionMessages.DATABASE_QUERY_FAILED + " (getTotalKillCount for " + username + ")", e);
        }
        return 0;
    }

    // Close connection when done
    public void close() {
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
                Gdx.app.log("DatabaseManager", "Database connection closed.");
            }
        } catch (SQLException e) {
            Gdx.app.error("DatabaseManager", "Error closing database connection.", e);
        } finally {
            connection = null;
            instance = null; // Reset singleton instance on close
        }
    }
}
