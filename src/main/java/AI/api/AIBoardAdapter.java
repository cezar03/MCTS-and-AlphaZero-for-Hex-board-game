package AI.api;

import game.core.Color;
import game.core.Player;
import java.util.List;

/**
 * Interface for board access by AI agents.
 * Decouples AI agents from concrete Board implementations,
 * allowing agents to work with any board that satisfies this interface.
 * 
 * @author Team 04
 */
public interface AIBoardAdapter {
    /**
     * Gets the color of a cell on the board.
     * @param row The row coordinate
     * @param col The column coordinate
     * @return The color of the cell (RED, BLACK, or EMPTY)
     */
    Color getCell(int row, int col);
    
    /**
     * Gets the size of the board.
     * @return The board dimension
     */
    int getSize();
    
    /**
     * Checks if the given coordinates are within board bounds.
     * @param row The row coordinate
     * @param col The column coordinate
     * @return true if coordinates are valid, false otherwise
     */
    boolean inBounds(int row, int col);
    
    /**
     * Checks if a cell is empty.
     * @param row The row coordinate
     * @param col The column coordinate
     * @return true if the cell is empty, false otherwise
     */
    boolean isEmpty(int row, int col);
    
    /**
     * Gets all legal moves for the board state.
     * @return A list of legal moves as [row, col] pairs
     */
    List<int[]> legalMoves();
    
    /**
     * Checks if the game is over.
     * @return true if a player has won, false otherwise
     */
    boolean isTerminal();
    
    /**
     * Checks if RED player has won.
     * @return true if RED has connected top to bottom, false otherwise
     */
    boolean redWins();
    
    /**
     * Checks if BLACK player has won.
     * @return true if BLACK has connected left to right, false otherwise
     */
    boolean blackWins();
    
    /**
     * Creates a copy of the current board state.
     * Used for simulations and lookahead calculations.
     * @return A new board with the same state
     */
    AIBoardAdapter copy();
    
    /**
     * Makes a move on the board.
     * @param row The row coordinate
     * @param col The column coordinate
     * @param player The player making the move
     * @return true if the move was legal and executed, false otherwise
     */
    boolean makeMove(int row, int col, Player player);
    
    /**
     * Places a RED stone at the given position.
     * Used by GameState for move simulation.
     * @param row The row coordinate
     * @param col The column coordinate
     * @param color The color
     */
    void getMoveRed(int row, int col, Color color);
    
    /**
     * Places a BLACK stone at the given position.
     * Used by GameState for move simulation.
     * @param row The row coordinate
     * @param col The column coordinate
     * @param color The color
     */
    void getMoveBlack(int row, int col, Color color);
    
    /**
     * Gets the neighboring cells for the given position.
     * Used by heuristics to evaluate move scores.
     * @param row The row coordinate
     * @param col The column coordinate
     * @return A list of neighboring [row, col] pairs
     */
    java.util.List<int[]> neighbors(int row, int col);
}











