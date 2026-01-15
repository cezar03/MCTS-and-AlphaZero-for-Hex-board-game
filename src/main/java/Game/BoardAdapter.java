package Game;

/**
 * An adapter class that bridges between the Board representation and a 2D integer
 * matrix representation of the game state.
 * <p>
 * This adapter maintains both representations in sync and provides convenient methods
 * for making moves, undoing moves, and querying game state. The matrix uses integer
 * encoding:
 * <ul>
 * <li>0 = EMPTY</li>
 * <li>1 = RED</li>
 * <li>2 = BLACK</li>
 * </ul>
 * <p>
 * This class is particularly useful for interfacing with AI systems or UI components
 * that prefer working with integer matrices rather than the Board's internal representation.
 */
public final class BoardAdapter {
    private final Board board;
    private final int[][] matrix;

    /**
     * Creates a new BoardAdapter wrapping the specified board.
     * Initializes the internal matrix representation based on the current board state.
     * 
     * @param board the Board instance to adapt
     */
    public BoardAdapter(Board board) {
        this.board = board;
        this.matrix = new int[board.getSize()][board.getSize()];
        updateMatrix();
    }


    /**
     * Synchronizes the internal matrix representation with the current board state.
     * Converts each cell's Color to its integer representation (0=EMPTY, 1=RED, 2=BLACK).
     * <p>
     * This method should be called after any direct modifications to the board to ensure
     * the matrix reflects the current state.
     */
    public void updateMatrix() {
        int n = board.getSize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                matrix[row][col] = colorToInt(board.getCell(row, col));
            }
        }
    }

    /**
     * Synchronizes the board with the current matrix representation.
     * Resets the board completely and then replays all stones from the matrix.
     * <p>
     * This method is useful when the matrix has been modified directly and the board
     * needs to be updated to match, such as after an undo operation.
     */
    public void updateBoard() {
        board.reset();
        int n = board.getSize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                int v = matrix[row][col];
                if (v == 1) {
                    board.getMoveRed(row, col, Color.RED);
                } else if (v == 2) {
                    board.getMoveBlack(row, col, Color.BLACK);
                }
            }
        }
    }

    /**
     * Returns the internal 2D integer matrix representation of the board.
     * 
     * @return the n×n matrix where each cell contains 0 (empty), 1 (red), or 2 (black)
     */
    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * Returns the underlying Board instance.
     * 
     * @return the Board being adapted
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Converts a Color enum value to its integer representation for the matrix.
     * Here 0 = empty, 1 = red, 2 = blsck
     * 
     * @param color the Color to convert
     * @return 0 for EMPTY, 1 for RED, 2 for BLACK
     */
    private int colorToInt(Color color) {
        switch (color) {
            case RED:
                return 1;
            case BLACK:
                return 2;
            case EMPTY:
            default:
                return 0;
        }
    }

    /**
     * Converts an integer value from the matrix to its corresponding Color enum.
     * 
     * @param value the integer to convert
     * @return EMPTY for 0, RED for 1, BLACK for 2, or EMPTY for any other value
     */
    private Color intToColor(int value) {
        switch (value) {
            case 1:
                return Color.RED;
            case 2:
                return Color.BLACK;
            case 0:
            default:
                return Color.EMPTY;
        }
    }

    /**
     * Attempts to make a move for the specified player at the given coordinates.
     * Updates both the matrix and board representations if the move is valid.
     * 
     * @param row the row coordinate for the move
     * @param col the column coordinate for the move
     * @param player the player making the move
     * @return true if the move was valid and successfully made, false if invalid
     */
    public boolean makeMove(int row, int col, Player player) {
        if (!Rules.validMove(board, row, col)) {
            return false;
        }

        // Update matrix
        matrix[row][col] = player.id;

        // Update board
        if (player == Player.RED) {
            board.getMoveRed(row, col, Color.RED);
        } else {
            board.getMoveBlack(row, col, Color.BLACK);
        }

        return true;
    }

    /**
     * Undoes a move at the specified position by clearing the cell in the matrix
     * and rebuilding the board state from the modified matrix.
     * <p>
     * Note: This method clears the cell and reconstructs the entire board to maintain
     * proper connectivity in the Union-Find structure.
     * 
     * @param row the row coordinate of the move to undo
     * @param col the column coordinate of the move to undo
     */
    public void undoMove(int row, int col) {
        matrix[row][col] = 0;
        updateBoard();
    }

    /**
     * Checks whether the game has ended.
     * 
     * @return true if either player has achieved a winning connection, false otherwise
     */
    public boolean isGameOver() {
        return board.isTerminal();
    }

    /**
     * Determines which player has won the game, if any.
     * 
     * @return Player.RED if red has won, Player.BLACK if black has won, or null if
     *         the game is not yet over
     */
    public Player getWinner() {
        if (board.redWins()) {
            return Player.RED;
        } else if (board.blackWins()) {
            return Player.BLACK;
        }
        return null;
    }

    /**
     * Resets both the matrix and board to their initial empty states.
     * All cells are cleared and the game can be started fresh.
     */
    public void reset() {
        int n = board.getSize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                matrix[row][col] = 0;
            }
        }
        board.reset();
    }

    /**
     * Returns the color of the stone at the specified position.
     * 
     * @param row the row coordinate
     * @param col the column coordinate
     * @return the Color of the cell (EMPTY, RED, or BLACK)
     */
    public Color getCellColor(int row, int col) {
        return board.getCell(row, col);
    }
}