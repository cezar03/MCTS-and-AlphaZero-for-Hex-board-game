package game.core;

public final class Rules {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Rules() {}

    /**
     * Checks whether the specified cell coordinates are within the board boundaries.
     * In Hex, all cells within the n×n grid are valid playable positions.
     * 
     * @param b the board to check against
     * @param row the row coordinate to validate
     * @param col the column coordinate to validate
     * @return true if the coordinates are within the board bounds, false otherwise
     */
    public static boolean validCell(Board b, int row, int col) {
        return b.inBounds(row, col);
    }

    /**
     * Checks whether a move to the specified cell is legal.
     * A move is valid if the cell is within bounds and currently empty.
     * 
     * @param b the board to check against
     * @param row the row coordinate of the proposed move
     * @param col the column coordinate of the proposed move
     * @return true if the move is legal (in bounds and cell is empty), false otherwise
     */
    public static boolean validMove(Board b, int row, int col) {
        return validCell(b, row, col) && b.isEmpty(row, col);
    }
    
    /**
     * Determines whether the pie rule (swap rule) is available for the current player.
     * <p>
     * The pie rule is a fairness mechanism in Hex that allows the second player to
     * choose to swap colors after the first player's opening move. This method can be
     * used to implement this optional rule.
     * 
     * @param plyCount the number of moves (plies) that have been played
     * @param toMove the player whose turn it is
     * @return true if the pie rule is available (typically only after the first move),
     *         false otherwise
     */
    public static boolean pieRuleAvailable(int plyCount, Player toMove) {
        return plyCount <= 1;
    }
}












