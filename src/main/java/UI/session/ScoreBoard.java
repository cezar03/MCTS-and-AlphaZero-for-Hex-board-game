package UI.session;

import game.core.Player;

/**
 * Tracks the score/wins for players throughout multiple games.
 * @author Team 04
 */
public class ScoreBoard {
    private int redWins = 0;
    private int blackWins = 0;
    private int totalGames = 0;

    /**
     * Records a win for the specified player.
     * @param winner The player who won
     */
    public void recordWin(Player winner) {
        if (winner == Player.RED) {
            redWins++;
        } else {
            blackWins++;
        }
        totalGames++;
    }

    /**
     * Gets the number of wins for RED player.
     * @return Number of wins for RED
     */
    public int getRedWins() {
        return redWins;
    }

    /**
     * Gets the number of wins for BLACK player.
     * @return Number of wins for BLACK
     */
    public int getBlackWins() {
        return blackWins;
    }

    /**
     * Gets the total number of games played.
     * @return Total games
     */
    public int getTotalGames() {
        return totalGames;
    }

    /**
     * Resets the scoreboard to zero wins.
     */
    public void reset() {
        redWins = 0;
        blackWins = 0;
        totalGames = 0;
    }

    /**
     * Gets the score as a formatted string.
     * @return Formatted score string
     */
    @Override
    public String toString() {
        return String.format("RED: %d | BLACK: %d | Games: %d", redWins, blackWins, totalGames);
    }
}











