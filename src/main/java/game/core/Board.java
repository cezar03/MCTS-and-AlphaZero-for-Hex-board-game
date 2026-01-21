package game.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the Hex game board, managing the grid state, move history, and 
 * connectivity checks.
 * <p>
 * This class uses a 1D array to store cell colors for performance and a 
 * {@link UnionFind} data structure to efficiently check for winning conditions 
 * (connections between opposite sides). It also maintains a history of moves 
 * to support undo functionality.
 */
public final class Board {
    private final int n;
    private final Color[] cells;
    private final UnionFind uf;
    private final int redTop, redBottom, blackLeft, blackRight;
    
    private static class MoveSnapshot {
        final int cellIndex;
        final Color previousColor;
        final int[] ufParent;
        final int[] ufRank;
        
        MoveSnapshot(int cellIndex, Color previousColor, UnionFind uf) {
            this.cellIndex = cellIndex;
            this.previousColor = previousColor;
            this.ufParent = Arrays.copyOf(uf.getParentArray(), uf.getParentArray().length);
            this.ufRank = Arrays.copyOf(uf.getRankArray(), uf.getRankArray().length);
        }
    }
    
    private final ArrayList<MoveSnapshot> moveHistory = new ArrayList<>();

    /**
     * Constructs a new empty Board of size n x n.
     * * @param n the dimension of the board (number of rows and columns)
     * @throws IllegalArgumentException if n is less than or equal to 0
     */
    public Board(int n) {
        if (n <= 0) throw new IllegalArgumentException("The board can t have less than 1 row, 1 column");
        this.n = n;
        this.cells = new Color[n * n];
        Arrays.fill(cells, Color.EMPTY);
        int unionFindSize = n * n + 4;
        this.uf = new UnionFind(unionFindSize);

        redTop = n * n;
        redBottom = n * n + 1;
        blackLeft = n * n + 2;
        blackRight = n * n + 3;
    }

    private Board(int n, Color[] cells, UnionFind uf) {
        this.n = n;
        this.cells = cells;
        this.uf = uf;
        this.redTop = n * n;
        this.redBottom = n * n + 1;
        this.blackLeft = n * n + 2;
        this.blackRight = n * n + 3;
    }

    /**
     * Creates a deep copy of the current board state.
     * <p>
     * This method copies the cell array and the underlying UnionFind structure,
     * allowing for safe simulation of moves without affecting the original board.
     * * @return a new Board instance identical to the current one
     */
    public Board fastCopy() {
        Color[] newCells = new Color[this.cells.length];
        System.arraycopy(this.cells, 0, newCells, 0, this.cells.length);
        UnionFind newUf = this.uf.copy();
        return new Board(this.n, newCells, newUf);
    }

    /**
     * Returns the size of the board (the 'n' in n x n).
     * * @return the number of rows (or columns)
     */
    public int getSize() { return n; }
    
    /**
     * Retrieves the color of the cell at the specified coordinates.
     * * @param row the row index (0-based)
     * @param column the column index (0-based)
     * @return the {@link Color} of the cell (RED, BLACK, or EMPTY)
     * @throws IndexOutOfBoundsException if the coordinates are invalid (via internal array access)
     */
    public Color getCell(int row, int column) { return cells[idx(row, column)];}
    
    /**
     * Places a RED stone at the specified coordinates.
     * <p>
     * This updates the board state and the connectivity graph.
     * * @param row the row index
     * @param column the column index
     * @param _ignored unused parameter, kept for signature compatibility
     * @throws IllegalStateException if the cell is not empty
     * @throws IndexOutOfBoundsException if coordinates are out of bounds
     */
    public void getMoveRed(int row, int column, Color _ignored) { terminate(row, column, Color.RED);}
    
    /**
     * Places a BLACK stone at the specified coordinates.
     * <p>
     * This updates the board state and the connectivity graph.
     * * @param row the row index
     * @param column the column index
     * @param _ignored unused parameter, kept for signature compatibility
     * @throws IllegalStateException if the cell is not empty
     * @throws IndexOutOfBoundsException if coordinates are out of bounds
     */
    public void getMoveBlack(int row, int column, Color _ignored) { terminate(row, column, Color.BLACK); }

    /**
     * Checks if the given coordinates represent a valid position on the board.
     * * @param row the row index
     * @param column the column index
     * @return true if the position is within the board limits, false otherwise
     */
    public boolean inBounds(int row, int column) { return row >= 0 && column >= 0 && row < n && column < n;}
    
    /**
     * Checks if a specific cell is currently empty.
     * * @param row the row index
     * @param column the column index
     * @return true if the cell is in bounds and its color is EMPTY, false otherwise
     */
    public boolean isEmpty(int row, int column) { return inBounds(row, column) && getCell(row, column) == Color.EMPTY;}
    
    /**
     * Checks if the Red player has won.
     * <p>
     * Red wins by connecting the top edge to the bottom edge.
     * * @return true if Red has a connecting path, false otherwise
     */
    public boolean redWins() { return uf.connected(redTop, redBottom); }

    /**
     * Checks if the Black player has won.
     * <p>
     * Black wins by connecting the left edge to the right edge.
     * * @return true if Black has a connecting path, false otherwise
     */
    public boolean blackWins() { return uf.connected(blackLeft, blackRight); }
    
    /**
     * Determines if the game has reached a terminal state (win/loss).
     * <p>
     * Since draws are impossible in Hex, this returns true if either Red or Black has won.
     * * @return true if a player has won, false otherwise
     */
    public boolean isTerminal() { return redWins() || blackWins();}

    /**
     * Generates a list of all valid moves available on the current board.
     * * @return a list of int arrays, where each array is {row, col} for an empty cell
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

    /**
     * Returns the number of moves currently stored in the history stack.
     * * @return the size of the move history
     */
    private void terminate(int row, int column, Color stone) {
        if (!inBounds(row, column)) throw new IndexOutOfBoundsException();

        int cellIndex = idx(row, column);
        if (cells[cellIndex] != Color.EMPTY) throw new IllegalStateException("Cell not empty");

        Color previousColor = cells[cellIndex];
        MoveSnapshot snapshot = new MoveSnapshot(cellIndex, previousColor, uf);
        moveHistory.add(snapshot);
        
        cells[cellIndex] = stone;
        List<int[]> list_of_neighbors = neighbors(row, column);
        
        for (var neighbor : list_of_neighbors) {
            int neighborIndex = idx(neighbor[0], neighbor[1]);
            if (cells[neighborIndex] == stone) { uf.union(cellIndex, neighborIndex);}
        }

        if (stone == Color.RED) {
            if (row == 0) uf.union(cellIndex, redTop);
            if (row == n - 1) uf.union(cellIndex, redBottom);
        } else if (stone == Color.BLACK) {
            if (column == 0) uf.union(cellIndex, blackLeft);
            if (column == n - 1) uf.union(cellIndex, blackRight);
        }
    }
    
    /**
     * Undoes the last move. Optimized for MCTS tree traversal.
     * @return true if a move was undone, false if no moves to undo
     */
    public boolean undoMove() {
        if (moveHistory.isEmpty()) return false;
        MoveSnapshot snapshot = moveHistory.remove(moveHistory.size() - 1);
        cells[snapshot.cellIndex] = snapshot.previousColor;
        uf.restore(snapshot.ufParent, snapshot.ufRank);
        return true;
    }

    /**
     * Returns the number of moves currently stored in the history stack.
     * * @return the size of the move history
     */
    public int getMoveHistorySize() {
        return moveHistory.size();
    }
    
    /**
     * Clears the history of moves.
     * <p>
     * This effectively prevents {@link #undoMove()} from being used for prior moves.
     * It is often used when synchronizing state or starting a fresh phase.
     */
    public void clearMoveHistory() {
        moveHistory.clear();
    }

    /**
     * Returns a list of valid neighboring coordinates for a given cell.
     * <p>
     * In a Hex grid, a cell has up to 6 neighbors.
     * * @param row the row index
     * @param column the column index
     * @return a list of int arrays representing neighbor coordinates {r, c}
     */
    public List<int[]> neighbors(int row, int column) {
        int[][] neighbor_deltas = { {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        List<int[]> neighbors = new ArrayList<>(6);
        for (var delta : neighbor_deltas) {
            int nr = row + delta[0];
            int nc = column + delta[1];
            if (inBounds(nr, nc)) neighbors.add(new int[]{nr, nc});
        }
        return neighbors;
    }

    /*
        * Converts 2D board coordinates to the corresponding 1D array index.
        * @param row the row index
        * @param column the column index
        * @return the index in the 1D cells array
     */
    private int idx(int row, int column) { return row * n + column; }

    /**
     * Forcibly sets a specific cell to EMPTY.
     * <p>
     * This does not update the UnionFind structure or move history automatically.
     * It is primarily used for resetting or undoing operations.
     * * @param row the row index
     * @param column the column index
     */
    public void clearCell(int row, int column) {
        if (inBounds(row, column)) cells[idx(row, column)] = Color.EMPTY;
    }

    /**
     * Resets the board to its initial state.
     * <p>
     * All cells are cleared to EMPTY, and the UnionFind structure is reset.
     */
    public void reset() {
        Arrays.fill(cells, Color.EMPTY);
        uf.reset();
    }
}










