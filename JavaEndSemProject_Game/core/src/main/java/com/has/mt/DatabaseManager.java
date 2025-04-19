package com.has.mt;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    // Use the same database name as your Workbench connection.
    private final String HOST = "jdbc:mysql://localhost:3306/";
    private final String DB_NAME = "gamedb";
    private final String DB_URL = HOST + DB_NAME + "?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASS = "P@rthubh3";  // Empty password

    // Private constructor for singleton
    private DatabaseManager() {
        try {
            // Load the MySQL JDBC driver (ensure it's in your classpath)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create properties for connection (explicitly set empty password)
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASS);

            // Optionally, you can include additional properties if needed:
            // props.setProperty("allowPublicKeyRetrieval", "true");

            // Optionally, if you want to create the database if it doesn't exist, you can connect to the server first:
            Connection tempConnection = DriverManager.getConnection(HOST + "?useSSL=false&serverTimezone=UTC", props);
            try (Statement stmt = tempConnection.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }
            tempConnection.close();

            // Now connect to the specified database
            connection = DriverManager.getConnection(DB_URL, props);
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Singleton instance getter
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Create the players table if it doesn't exist
    public void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS players ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "name VARCHAR(100) UNIQUE,"
            + "killCount INT"
            + ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add a new player with initial killCount
    public void addPlayer(String name, int killCount) {
        String insertSQL = "INSERT IGNORE INTO players(name, killCount) VALUES(?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, killCount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update player's kill count
    public void updateKillCount(String name, int killCount) {
        String updateSQL = "UPDATE players SET killCount = ? WHERE name = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setInt(1, killCount);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve player's kill count
    public int getKillCount(String name) {
        String querySQL = "SELECT killCount FROM players WHERE name = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("killCount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Close connection when done
    public void close() {
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
