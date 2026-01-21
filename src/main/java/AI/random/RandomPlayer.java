package AI.random;

import AI.api.AIAgent;
import AI.api.AIBoardAdapter;
import game.core.Color;
import game.core.Move;
import game.core.Player;

/**
 * A simple AI agent implementation that selects moves uniformly at random.
 * <p>
 * This agent identifies all currently legal moves on the board and picks one
 * without any strategic heuristic. It is primarily used as a baseline for comparison
 * or for easy difficulty levels.
 */
public class RandomPlayer implements AIAgent {
    private final Player randomPlayer;

    /**
     * Constructs a RandomPlayer for the specified side.
     * * @param randomPlayer the player (RED or BLACK) this agent controls
     */
    public RandomPlayer(Player randomPlayer) {
        this.randomPlayer = randomPlayer;
    }

    /**
     * Selects a random legal move from the board.
     * * @param board the current board state
     * @param currentPlayer the player whose turn it is
     * @return a randomly selected {@link Move}, or null if no moves are possible
     */
    @Override
    public Move getBestMove(AIBoardAdapter board, Player currentPlayer) {
        int n = board.getSize();
        int empties = 0;

        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (board.getCell(r, c) == Color.EMPTY) empties++;

        if (empties == 0) return null;

        int k = (int) (Math.random() * empties);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.getCell(r, c) == Color.EMPTY) { if (k-- == 0) return Move.get(r, c); }
            }
        }
        return null;
    }

<<<<<<< HEAD
    /**
     * Returns the player this agent represents.
     * * @return the Player enum
     */
    @Override
    public Player getPlayer() {
        return randomPlayer;
    }

    /**
     * Checks if this agent controls the given player.
     * * @param player the player to check
     * @return true if the IDs match
     */
    @Override
    public boolean controlsPlayer(Player player) {
        return this.randomPlayer == player;
    }
=======
    @Override public Player getPlayer() { return randomPlayer;}
    @Override public boolean controlsPlayer(Player player) { return this.randomPlayer == player; }
>>>>>>> 1165bedc5af5867e936278ee2626c1ff7663bbd5
}











