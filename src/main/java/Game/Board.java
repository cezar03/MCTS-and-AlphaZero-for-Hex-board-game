package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the game board for a Hex game, managing cell states, stone placement,
 * and win condition detection.
 * <p>
 * The board is an n×n rhombus of hexagonal cells where:
 * <ul>
 * <li>RED player aims to connect the top edge (row 0) to the bottom edge (row n-1)</li>
 * <li>BLACK player aims to connect the left edge (column 0) to the right edge (column n-1)</li>
 * </ul>
 * <p>
 * This class uses a Union-Find data structure to efficiently detect when a player
 * has created a winning connection. Virtual edge nodes are maintained to represent
 * the goal edges for each player, and stones are unioned with these edges when placed
 * on boundary rows/columns.
 * <p>
 * The board uses a flattened 1D array internally for cell storage, with helper methods
 * to convert between 2D coordinates and array indices.
 */
public final class Board {
    private final int n;
    private final Color[] cells;
    private final UnionFind uf;
    private final int redTop, redBottom, blackLeft, blackRight;

    /**
     * Creates a new Hex game board with the specified size.
     * The board will be an n×n grid of hexagonal cells, all initially empty.
     * Four virtual nodes are created to represent the goal edges for win detection.
     * 
     * @param n the size of the board (number of rows and columns)
     * @throws IllegalArgumentException if n is less than or equal to 0
     */
    public Board(int n) {
        if (n <= 0) throw new IllegalArgumentException("The board can t have less than 1 row, 1 column");
        this.n = n;
        this.cells = new Color[n * n];
        Arrays.fill(cells, Color.EMPTY);
        int unionFindSize = n * n + 4;
        this.uf = new UnionFind(unionFindSize);

        //Indices for edge nodes
        redTop = n * n;
        redBottom = n * n + 1;
        blackLeft = n * n + 2;
        blackRight = n * n + 3;
    }

    /* Get methods */

    /**
     * Returns the size of the board.
     * 
     * @return the number of rows (and columns) in this n×n board
     */
    public int getSize() {
        return n;
    }
    
    /**
     * Returns the color of the stone at the specified position, or EMPTY if unoccupied.
     * 
     * @param row the row coordinate (0 to n-1)
     * @param column the column coordinate (0 to n-1)
     * @return the Color of the cell at the given position
     */
    public Color getCell(int row, int column) {
        return cells[idx(row, column)];
    }

    /**
     * Places a red stone at the specified position on the board.
     * This method handles connectivity updates and win condition checking.
     * 
     * @param row the row coordinate where the red stone will be placed
     * @param column the column coordinate where the red stone will be placed
     * @param _ignored the color parameter (ignored, kept for interface consistency)
     * @throws IndexOutOfBoundsException if the position is outside the board
     * @throws IllegalStateException if the cell is already occupied
     */
    public void getMoveRed(int row, int column, Color _ignored) {
        terminate(row, column, Color.RED);
    }

    /**
     * Places a black stone at the specified position on the board.
     * This method handles connectivity updates and win condition checking.
     * 
     * @param row the row coordinate where the black stone will be placed
     * @param column the column coordinate where the black stone will be placed
     * @param _ignored the color parameter (ignored, kept for interface consistency)
     * @throws IndexOutOfBoundsException if the position is outside the board
     * @throws IllegalStateException if the cell is already occupied
     */
    public void getMoveBlack(int row, int column, Color _ignored) {
        terminate(row, column, Color.BLACK);
    }

    /* Helpers for UI and AI */

    /**
     * Checks whether the specified coordinates are within the board boundaries.
     * 
     * @param row the row coordinate to check
     * @param column the column coordinate to check
     * @return true if the coordinates are within bounds [0, n-1] for both row and column,
     *         false otherwise
     */
    public boolean inBounds(int row, int column) {
        return row >= 0 && column >= 0 && row < n && column < n;
    }

    /**
     * Checks whether the specified cell is empty and within board boundaries.
     * 
     * @param row the row coordinate to check
     * @param column the column coordinate to check
     * @return true if the coordinates are in bounds and the cell contains no stone,
     *         false otherwise
     */
    public boolean isEmpty(int row, int column) {
        return inBounds(row, column) && getCell(row, column) == Color.EMPTY;
    }

    /**
     * Creates a deep copy of the given board, preserving all stone placements and
     * connectivity information.
     * This method is primarily used for simulation purposes in MCTS (Monte Carlo Tree Search).
     * 
     * @param original the board to copy
     * @return a new Board instance with the same state as the original
     */
    public Board copyBoard(Board original) {
        int n = original.getSize();
        Board copy = new Board(n);
        
        // Copy all placed stones
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                Color cellColor = original.getCell(row, col);
                if (cellColor == Color.RED) {
                    copy.getMoveRed(row, col, Color.RED);
                } else if (cellColor == Color.BLACK) {
                    copy.getMoveBlack(row, col, Color.BLACK);
                }
            }
        }
        
        return copy;
    }

    /**
     * Checks whether the red player has achieved a winning connection.
     * Red wins by connecting the top edge (row 0) to the bottom edge (row n-1).
     * 
     * @return true if red's top and bottom virtual edge nodes are connected,
     *         false otherwise
     */
    public boolean redWins() {
        return uf.find(redTop) == uf.find(redBottom);
    }

    /**
     * Checks whether the black player has achieved a winning connection.
     * Black wins by connecting the left edge (column 0) to the right edge (column n-1).
     * 
     * @return true if black's left and right virtual edge nodes are connected,
     *         false otherwise
     */
    public boolean blackWins() {
        return uf.find(blackLeft) == uf.find(blackRight);
    }

    /**
     * Checks whether the game has ended (i.e., one player has won).
     * 
     * @return true if either red or black has achieved a winning connection,
     *         false otherwise
     */
    public boolean isTerminal() {
        return redWins() || blackWins();
    }

    /**
     * Returns a list of all legal moves (empty cells) on the board.
     * Each move is represented as an int array [row, column].
     * 
     * @return a list of coordinate pairs representing all empty cells on the board
     */
    public List<int[]> legalMoves() {
        List<int[]> legalMovesList = new ArrayList<>();
        for (int row = 0; row < n; row++) {
            for (int column = 0; column < n; column++) {
                if (isEmpty(row, column)) {
                    legalMovesList.add(new int[]{row, column});
                }
            }
        }
        return legalMovesList;
    }

    /* Placing stones */

    /**
     * Internal method to place a stone at the specified position and update connectivity.
     * <p>
     * This method:
     * <ol>
     * <li>Validates the position is in bounds and empty</li>
     * <li>Places the stone in the cell</li>
     * <li>Unions the cell with all same-colored neighboring stones</li>
     * <li>Unions the cell with appropriate virtual edge nodes if on a boundary</li>
     * </ol>
     * 
     * @param row the row coordinate for stone placement
     * @param column the column coordinate for stone placement
     * @param stone the color of stone to place (RED or BLACK)
     * @throws IndexOutOfBoundsException if the position is outside the board
     * @throws IllegalStateException if the cell is already occupied
     */
    private void terminate(int row, int column, Color stone) {
        if (!inBounds(row, column)) {
            throw new IndexOutOfBoundsException();
        }

        int cellIndex = idx(row, column);
        if (cells[cellIndex] != Color.EMPTY) {
            throw new IllegalStateException("Cell not empty");
        }
        cells[cellIndex] = stone;
        List<int[]> list_of_neighbors = neighbors(row, column);
        // Union with same-colored neighbors
        for (var neighbor : list_of_neighbors) {
            int neighborRow    = neighbor[0];
            int neighborColumn = neighbor[1];
            int neighborIndex  = idx(neighborRow, neighborColumn);
            if (inBounds(neighborRow, neighborColumn) && cells[neighborIndex] == stone) {
                uf.union(cellIndex, neighborIndex);
            }
        }

        // Union with virtual edge nodes (for win detection)
        if (stone == Color.RED) {
            if (row == 0) {
                uf.union(cellIndex, redTop);
            }
            if (row == n - 1) {
                uf.union(cellIndex, redBottom);
            }
        } else if (stone == Color.BLACK) {
            if (column == 0) {
                uf.union(cellIndex, blackLeft);
            }
            if (column == n - 1) {
                uf.union(cellIndex, blackRight);
            }
        }
    }

    /**
     * Returns the coordinates of all valid neighboring cells for the specified position.
     * <p>
     * In a pointy-top hexagonal grid laid out as an n×n rhombus, each cell has up to
     * six neighbors. The returned list only includes neighbors that are within the
     * board boundaries.
     * 
     * @param row the row coordinate of the cell
     * @param column the column coordinate of the cell
     * @return a list of int arrays [row, column] representing valid neighboring positions
     */
    public List<int[]> neighbors(int row, int column) {
        int[][] neighbor_deltas = { {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        List<int[]> neighbors = new ArrayList<>(6);
        for (var delta : neighbor_deltas) {
            int neighborRow = row + delta[0];
            int neighborColumn = column + delta[1];
            if (inBounds(neighborRow, neighborColumn)) {
                neighbors.add(new int[]{neighborRow, neighborColumn});
            }
        }
        return neighbors;
    }

    /**
     * Converts 2D board coordinates to a 1D array index.
     * <p>
     * The mapping uses row-major order: index = row * n + column
     * 
     * @param row the row coordinate
     * @param column the column coordinate
     * @return the corresponding index in the flattened cells array
     */
    private int idx(int row, int column) {
        return row * n + column;
    }

    /**
     * Removes any stone from the specified cell, making it empty.
     * This is a helper method for undoing moves.
     * <p>
     * Note: This method only clears the cell state; it does not update the Union-Find
     * connectivity structure. For full board consistency after clearing cells, use the
     * reset() method or rebuild connectivity.
     * 
     * @param row the row coordinate of the cell to clear
     * @param column the column coordinate of the cell to clear
     */
    public void clearCell(int row, int column) {
        if (inBounds(row, column)) {
            cells[idx(row, column)] = Color.EMPTY;
        }
    }

    /**
     * Resets the board to its initial empty state, clearing all stones and
     * resetting the connectivity structure.
     * This method sets all cells to EMPTY and reinitializes the Union-Find structure.
     */
    public void reset() {
        Arrays.fill(cells, Color.EMPTY); // clear stones
        uf.reset();                      // clear connectivity
    }
}

