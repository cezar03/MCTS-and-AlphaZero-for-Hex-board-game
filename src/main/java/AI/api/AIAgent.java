package ai.api;

import game.core.Move;
import game.core.Player;

/**
 * Interface for all AI agents that can play the game.
 * Decoupled from concrete Board implementations via AIBoardAdapter.
 * Supports optional lifecycle management for agent initialization and cleanup.
 * Please dont move this class or refactor it as it is working perfectly fine,
 * In case of moving this class to another package will cause a lot of issues with imports and dependencies,
 * I couldnt figure it out for hours so let it be here where it is.
 * 
 * @author Team 04
 */
public interface AIAgent {
    /**
     * Determines the best move for this agent.
     * @param board The current game board state (via AIBoardAdapter interface)
     * @param currentPlayer The player whose turn it is
     * @return The chosen move, or null if no moves are available
     */
    Move getBestMove(AIBoardAdapter board, Player currentPlayer);
    
    /**
     * Gets the player this agent represents.
     * @return The player (RED or BLACK)
     */
    Player getPlayer();
    
    /**
     * Checks if the given player is controlled by this agent.
     * @param player The player to check
     * @return true if this agent controls the player, false otherwise
     */
    boolean controlsPlayer(Player player);
    
    /**
     * Optional lifecycle method called when the agent is set up for a game.
     * Can be overridden by implementations that need initialization.
     * Default implementation does nothing.
     */
    default void initialize() {
        // Default: no initialization needed
    }
    
    /**
     * Optional lifecycle method called when the agent is removed from a game.
     * Can be overridden by implementations that need cleanup.
     * Default implementation does nothing.
     */
    default void cleanup() {
        // Default: no cleanup needed
    }
}










