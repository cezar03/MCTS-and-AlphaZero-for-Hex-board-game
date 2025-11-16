package AI;

import AI.mcts.MCTS;
import AI.mcts.Node;
import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import Game.Board;
import Game.Player;

/**
 * AIPlayer class that uses Monte Carlo Tree Search (MCTS) to determine the best move.
 * Integrates the AI system with the game.
 * 
 * @author Team 04
 */
public class AIPlayer {
    private final MCTS mcts;
    private final Player aiPlayer;
    private final int iterations;

    /**
     * Constructor for AIPlayer
     * @param aiPlayer The player this AI represents (RED or BLACK)
     * @param iterations The number of MCTS iterations to perform
     */
    public AIPlayer(Player aiPlayer, int iterations) {
        this.aiPlayer = aiPlayer;
        this.iterations = iterations;
        this.mcts = new MCTS(iterations);
    }

    /**
     * Determines the best move for the AI player using MCTS.
     * 
     * @param board The current game board state
     * @param currentPlayer The player whose turn it is
     * @return The best move found by the MCTS algorithm, or null if no moves are available
     */
    public Move getBestMove(Board board, Player currentPlayer) {
        GameState gameState = new GameState(board, currentPlayer);

        if (gameState.getLegalMoves().isEmpty()) {
            return null;
        }

        Node root = new Node(null, null, currentPlayer.id);

        Node bestNode = mcts.search(root, gameState);

        if (bestNode == null || bestNode.move == null) {
            return gameState.getLegalMoves().get(0);
        }

        return bestNode.move;
    }

    /**
     * Gets the player this AI represents.
     * @return The player (RED or BLACK)
     */
    public Player getAIPlayer() {
        return aiPlayer;
    }

    /**
     * Gets the number of iterations used in MCTS search.
     * @return The iteration count
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Checks if the given player is controlled by this AI.
     * @param player The player to check
     * @return true if this AI controls the player, false otherwise
     */
    public boolean controlsPlayer(Player player) {
        return this.aiPlayer == player;
    }
}
