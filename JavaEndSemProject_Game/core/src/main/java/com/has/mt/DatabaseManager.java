package com.has.mt;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    // singleton instance
    private static DatabaseManager instance;

    // JDBC connection
    private Connection connection;

    // database credentials
    private static final String HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "gamedb";
    private static final String USER = "root";
    private static final String PASS = "P@rthubh3"; // subject to change 

    // full DB URL with parameters
    private static final String DB_URL = HOST + DB_NAME + "?useSSL=false&serverTimezone=UTC";

    // private constructor - initializes DB connection
    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASS);

            // connect to server (without DB) first to ensure DB exists
            try (Connection tempConn = DriverManager.getConnection(HOST + "?useSSL=false&serverTimezone=UTC", props);
                 Statement stmt = tempConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            // connect to the target database
            connection = DriverManager.getConnection(DB_URL, props);

            // initialize tables if needed
            initializeDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // singleton instance getter
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ensures the `players` table exists
    private void initializeDatabase() {
        final String sql = "CREATE TABLE IF NOT EXISTS players ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(100) UNIQUE, "
                + "killCount INT"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // inserts a new player into the table
    public void addPlayer(String name, int killCount) {
        final String sql = "INSERT IGNORE INTO players(name, killCount) VALUES(?, ?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, killCount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // updates kill count for a specific player
    public void updateKillCount(String name, int killCount) {
        final String sql = "UPDATE players SET killCount = ? WHERE name = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, killCount);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // fetches kill count for a given player
    public int getKillCount(String name) {
        final String sql = "SELECT killCount FROM players WHERE name = ?;";
        int kills = 0;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                kills = rs.getInt("killCount");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kills;
    }

    // closes the database connection
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
