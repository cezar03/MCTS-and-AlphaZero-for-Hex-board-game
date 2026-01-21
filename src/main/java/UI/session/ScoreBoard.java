package UI.session;

import game.core.Player;

/**
 * Maintains cumulative win statistics across multiple games.
 *
 * <p>The {@code ScoreBoard} tracks:</p>
 * <ul>
 *   <li>Total wins for the RED player</li>
 *   <li>Total wins for the BLACK player</li>
 *   <li>Total number of games played</li>
 * </ul>
 *
 * <p>This class is typically used at the session level (e.g. during a match
 * series or tournament) rather than for a single game instance.</p>
 *
 * <p>All counters are updated through {@link #recordWin(Player)} and can be
 * reset using {@link #reset()}.</p>
 *
 * @author Team 04
 */
public class ScoreBoard {

    /**
     * Number of wins recorded for the RED player.
     */
    private int redWins = 0;

    /**
     * Number of wins recorded for the BLACK player.
     */
    private int blackWins = 0;

    /**
     * Total number of games played.
     */
    private int totalGames = 0;

    /**
     * Records a win for the specified player and increments
     * the total number of games played.
     *
     * @param winner the player who won the game
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
     * Returns the number of wins for the RED player.
     *
     * @return the number of RED wins
     */
    public int getRedWins() {
        return redWins;
    }

    /**
     * Returns the number of wins for the BLACK player.
     *
     * @return the number of BLACK wins
     */
    public int getBlackWins() {
        return blackWins;
    }

    /**
     * Returns the total number of games played.
     *
     * @return the total number of games
     */
    public int getTotalGames() {
        return totalGames;
    }

    /**
     * Resets all counters to zero.
     *
     * <p>After calling this method, no games are considered to have been
     * played and no wins are recorded.</p>
     */
    public void reset() {
        redWins = 0;
        blackWins = 0;
        totalGames = 0;
    }

    /**
     * Returns a human-readable summary of the current scoreboard state.
     *
     * @return a formatted string in the form
     *         {@code "RED: x | BLACK: y | Games: z"}
     */
    @Override
    public String toString() {
        return String.format("RED: %d | BLACK: %d | Games: %d",
                redWins, blackWins, totalGames);
    }
}












