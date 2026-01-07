package Game;

import AI.AiPlayer.AIBoardAdapter;
import java.util.List;

public final class BoardAdapter implements AIBoardAdapter {
    private final Board board;
    private final int[][] matrix;

    public BoardAdapter(Board board) {
        this.board = board;
        this.matrix = new int[board.getSize()][board.getSize()];
        updateMatrix();
    }


    //Updates the 2D matrix  based on the current state of the board
    public void updateMatrix() {
        int n = board.getSize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                matrix[row][col] = colorToInt(board.getCell(row, col));
            }
        }
    }

    //Updates the board within the changes in the matrix
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

    public int[][] getMatrix() {
        return matrix;
    }

    public Board getBoard() {
        return board;
    }


    //Converts Color enum to integer representation for matrix
    //Where 0 = empty, 1 = red, 2 = blsck

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

    public void undoMove(int row, int col) {
        matrix[row][col] = 0;
        updateBoard();
    }


    public boolean isGameOver() {
        return board.isTerminal();
    }


    public Player getWinner() {
        if (board.redWins()) {
            return Player.RED;
        } else if (board.blackWins()) {
            return Player.BLACK;
        }
        return null;
    }

    //Resets both the matrix and the board
    public void reset() {
        int n = board.getSize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                matrix[row][col] = 0;
            }
        }
        board.reset();
    }

    public Color getCellColor(int row, int col) {
        return board.getCell(row, col);
    }
    
    /**
     * Creates a deep copy of the board adapter for simulations.
     * Implements AIBoardAdapter.copy().
     */
    @Override
    public AIBoardAdapter copy() {
        Board boardCopy = board.copyBoard(board);
        BoardAdapter adapterCopy = new BoardAdapter(boardCopy);
        // Copy the matrix state
        int n = this.matrix.length;
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                adapterCopy.matrix[row][col] = this.matrix[row][col];
            }
        }
        return adapterCopy;
    }
    
    /**
     * Gets all legal moves on the board.
     * Implements AIBoardAdapter.legalMoves().
     */
    @Override
    public List<int[]> legalMoves() {
        return board.legalMoves();
    }
    
    /**
     * Checks if the game is over (terminal state).
     * Implements AIBoardAdapter.isTerminal().
     */
    @Override
    public boolean isTerminal() {
        return board.isTerminal();
    }
    
    /**
     * Checks if RED player has won.
     * Implements AIBoardAdapter.redWins().
     */
    @Override
    public boolean redWins() {
        return board.redWins();
    }
    
    /**
     * Checks if BLACK player has won.
     * Implements AIBoardAdapter.blackWins().
     */
    @Override
    public boolean blackWins() {
        return board.blackWins();
    }
    
    /**
     * Gets the size of the board.
     * Implements AIBoardAdapter.getSize().
     */
    @Override
    public int getSize() {
        return board.getSize();
    }
    
    /**
     * Checks if coordinates are in bounds.
     * Implements AIBoardAdapter.inBounds().
     */
    @Override
    public boolean inBounds(int row, int col) {
        return board.inBounds(row, col);
    }
    
    /**
     * Checks if a cell is empty.
     * Implements AIBoardAdapter.isEmpty().
     */
    @Override
    public boolean isEmpty(int row, int col) {
        return board.isEmpty(row, col);
    }
    
    /**
     * Gets the color of a cell (AIBoardAdapter implementation).
     * Implements AIBoardAdapter.getCell().
     */
    @Override
    public Color getCell(int row, int col) {
        return board.getCell(row, col);
    }
    
    /**
     * Makes a move on the board (AIBoardAdapter implementation).
     * Implements AIBoardAdapter.makeMove().
     */
    @Override
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
     * Places a RED stone at the given position.
     * Implements AIBoardAdapter.getMoveRed().
     */
    @Override
    public void getMoveRed(int row, int col, Color color) {
        board.getMoveRed(row, col, color);
    }
    
    /**
     * Places a BLACK stone at the given position.
     * Implements AIBoardAdapter.getMoveBlack().
     */
    @Override
    public void getMoveBlack(int row, int col, Color color) {
        board.getMoveBlack(row, col, color);
    }
    
    /**
     * Gets the neighboring cells for the given position.
     * Implements AIBoardAdapter.neighbors().
     */
    @Override
    public List<int[]> neighbors(int row, int col) {
        return board.neighbors(row, col);
    }
}