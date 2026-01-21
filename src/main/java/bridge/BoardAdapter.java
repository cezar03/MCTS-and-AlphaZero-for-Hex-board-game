package bridge;

import java.util.List;

import AI.api.AIBoardAdapter;
import game.core.Board;
import game.core.Color;
import game.core.Player;
import game.core.Rules;

/**
 * Adapter that exposes game.core.Board through AIBoardAdapter for AI agents.
 * Lives in bridge to avoid a Game -> AI dependency.
 */
public final class BoardAdapter implements AIBoardAdapter {
    private final Board board;
    private final int[][] matrix;

    /**
     * Constructs an adapter for the given board.
     * <p>
     * Initializes the internal integer matrix based on the board's current state.
     * * @param board the Board instance to adapt
     */
    public BoardAdapter(Board board) {
        this.board = board;
        this.matrix = new int[board.getSize()][board.getSize()];
        updateMatrix();
    }

    /**
     * AIBoardAdapter methods
     * @see AI.api.AIBoardAdapter
    */
    @Override
    public Color getCell(int row, int col) {
        return board.getCell(row, col);
    }

    /**
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public int getSize() {
        return board.getSize();
    }

    /**
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public boolean inBounds(int row, int col) {
        return board.inBounds(row, col);
    }

    /**
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public boolean isEmpty(int row, int col) {
        return board.isEmpty(row, col);
    }

    /**  
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public List<int[]> legalMoves() {
        return board.legalMoves();
    }

    /**  
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public boolean isTerminal() {
        return board.isTerminal();
    }

    /**  
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public boolean redWins() {
        return board.redWins();
    }

    /**  
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public boolean blackWins() {
        return board.blackWins();
    }

    /**
     * Creates a deep copy of this adapter and its underlying board.
     * * @return a new BoardAdapter instance with copied state
     */
    @Override
    public AIBoardAdapter copy() {
        Board b2 = board.fastCopy();
        b2.clearMoveHistory();
        return new BoardAdapter(b2);
    }

    /**
     * Executes a move on the board and updates the internal matrix representation.
     * * @param row the row coordinate
     * @param col the column coordinate
     * @param player the player making the move
     * @return true if the move was valid, false otherwise
     */
    @Override
    public boolean makeMove(int row, int col, Player player) {
        if (!Rules.validMove(board, row, col)) return false;

        if (player == Player.RED) {
            board.getMoveRed(row, col, null);
            matrix[row][col] = 1;
        } else {
            board.getMoveBlack(row, col, null);
            matrix[row][col] = 2;
        }
        return true;
    }

    /**  
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public void getMoveRed(int row, int col, Color colorIgnored) {
        board.getMoveRed(row, col, null);
        matrix[row][col] = 1;
    }

    /**  
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public void getMoveBlack(int row, int col, Color colorIgnored) {
        board.getMoveBlack(row, col, null);
        matrix[row][col] = 2;
    }

    /**  
     * @see AI.api.AIBoardAdapter
     */
    @Override
    public List<int[]> neighbors(int row, int col) {
        return board.neighbors(row, col);
    }

    /**
     * Returns the current board state as a 2D integer array.
     * <p>
     * 0 = Empty, 1 = Red, 2 = Black.
     * * @return the integer matrix
     */
    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * @return the underlying Board instance
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Synchronizes the internal integer matrix to match the current state of the Board object.
     */
    public void updateMatrix() {
        int n = board.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                matrix[r][c] = colorToInt(board.getCell(r, c));
            }
        }
    }

    /**
     * Synchronizes the underlying Board object to match the current state of the integer matrix.
     * <p>
     * This rebuilds the board state and clears move history.
     */
    public void updateBoard() {
        board.reset();
        int n = board.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int v = matrix[r][c];
                if (v == 1) board.getMoveRed(r, c, null);
                else if (v == 2) board.getMoveBlack(r, c, null);
            }
        }
        board.clearMoveHistory();
    }

    /**
     * Manually undoes a move at the specified coordinate.
     * <p>
     * This sets the position to 0 in the matrix and refreshes the board state.
     * * @param row the row coordinate
     * @param col the column coordinate
     */
    public void undoMove(int row, int col) {
        if (!inBounds(row, col)) return;
        matrix[row][col] = 0;
        updateBoard();
    }

    /**
     * Checks if the game has reached a terminal state.
     * * @return true if the game is over
     */
    public boolean isGameOver() {
        return board.isTerminal();
    }

    /**
     * Determines the winner of the game, if any.
     * * @return {@link Player#RED} or {@link Player#BLACK} if there is a winner, null otherwise
     */
    public Player getWinner() {
        if (board.redWins()) return Player.RED;
        if (board.blackWins()) return Player.BLACK;
        return null;
    }

    /**
     * Resets both the matrix and the underlying board to an empty state.
     */
    public void reset() {
        int n = board.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                matrix[r][c] = 0;
            }
        }
        board.reset();
        board.clearMoveHistory();
    }

    /**
     * Converts a Color enum to its corresponding integer representation.
     * <p>
     * RED = 1, BLACK = 2, EMPTY = 0.
     * @param color
     * @return the integer value
     */
    private int colorToInt(Color color) {
        return switch (color) {
            case RED -> 1;
            case BLACK -> 2;
            case EMPTY -> 0;
        };
    }
}











