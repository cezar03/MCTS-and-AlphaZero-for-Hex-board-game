package game.view;

import java.util.List;

import game.core.Board;
import game.core.Color;
import game.core.Player;
import game.core.Rules;

/**
 * A wrapper view for the {@link Board} class that exposes state and logic 
 * required for the UI, without directly exposing internal AI mechanisms.
 * <p>
 * This acts as an intermediary or facade, ensuring the UI interacts with the 
 * board through a defined interface.
 */
public final class BoardStateView {
    private final Board board;

    /**
     * Constructs a view for the specified board.
     * * @param board the underlying Board instance to view
     */
    public BoardStateView(Board board) {
        this.board = board;
    }

    /**
     * Retrieves the underlying Board instance.
     * * @return the Board object
     */
    public Board getBoard() { return board; }

    /**
     * Returns the size of the board.
     * * @return the dimension n
     */
    public int getSize() { return board.getSize(); }

    /**
     * Gets the color of the cell at the specified coordinates.
     * * @param r the row index
     * @param c the column index
     * @return the Color of the cell
     */
    public Color getCell(int r, int c) { return board.getCell(r, c); }
    
    /**
     * Checks if the coordinates are within the board boundaries.
     * * @param r the row index
     * @param c the column index
     * @return true if in bounds, false otherwise
     */
    public boolean inBounds(int r, int c) { return board.inBounds(r, c); }
    
    /**
     * Checks if the cell at the specified coordinates is empty.
     * * @param r the row index
     * @param c the column index
     * @return true if the cell is empty
     */
    public boolean isEmpty(int r, int c) { return board.isEmpty(r, c); }
    
    /**
     * Retrieves a list of all legal moves (empty cells) on the board.
     * * @return a list of int arrays {row, col}
     */
    public List<int[]> legalMoves() { return board.legalMoves(); }
    
    /**
     * Gets the valid neighbors of a specific cell.
     * * @param r the row index
     * @param c the column index
     * @return a list of coordinate arrays {row, col} for adjacent cells
     */
    public List<int[]> neighbors(int r, int c) { return board.neighbors(r, c); }
    
    /**
     * Checks if the game has ended.
     * * @return true if either player has won
     */
    public boolean isTerminal() { return board.isTerminal(); }
    
    /**
     * Checks if Red has won the game.
     * * @return true if Red connects top and bottom
     */
    public boolean redWins() { return board.redWins(); }

    /**
     * Checks if Black has won the game.
     * * @return true if Black connects left and right
     */
    public boolean blackWins() { return board.blackWins(); }
    
    /**
     * Attempts to make a move on the board for the specified player.
     * * @param r the row coordinate
     * @param c the column coordinate
     * @param p the Player attempting the move
     * @return true if the move was valid and executed, false otherwise
     */
    public boolean makeMove(int r, int c, Player p) {
        if (!Rules.validMove(board, r, c)) return false;
        if (p == Player.RED) board.getMoveRed(r, c, null);
        else board.getMoveBlack(r, c, null);
        return true;
    }
}











