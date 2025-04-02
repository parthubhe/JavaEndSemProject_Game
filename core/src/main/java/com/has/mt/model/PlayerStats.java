package com.has.mt.model;

/**
 * Represents the statistics for a player stored in the database.
 */
public class PlayerStats {
    public String username;
    public int killCount;
    public int highestScore;
    public String lastOutcome; // "WIN", "LOSE", "NONE"
    public int totalWins;
    public int totalLosses;

    public PlayerStats(String username, int killCount, int highestScore, String lastOutcome, int totalWins, int totalLosses) {
        this.username = username;
        this.killCount = killCount;
        this.highestScore = highestScore;
        this.lastOutcome = lastOutcome;
        this.totalWins = totalWins;
        this.totalLosses = totalLosses;
    }

    @Override
    public String toString() {
        return String.format("User: %s, Score: %d, Kills: %d, Wins: %d, Losses: %d",
            username, highestScore, killCount, totalWins, totalLosses);
    }
}
