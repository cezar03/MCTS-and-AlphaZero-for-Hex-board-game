package Game;

import java.util.List;

import AI.AiPlayer.AIBoardAdapter;

/**
 * Adapter that exposes Game.Board through AIBoardAdapter for AI agents.
 * NOTE: This introduces a dependency from Game -> AI (not ideal), but compiles fast.
 */
public final class BoardAdapter implements AIBoardAdapter {
    private final Board board;
    private final int[][] matrix;

    public BoardAdapter(Board board) {
        this.board = board;
        this.matrix = new int[board.getSize()][board.getSize()];
        updateMatrix();
    }

    // ---------------- AIBoardAdapter implementation ----------------

    @Override
    public Color getCell(int row, int col) {
        return board.getCell(row, col);
    }

    @Override
    public int getSize() {
        return board.getSize();
    }

    @Override
    public boolean inBounds(int row, int col) {
        return board.inBounds(row, col);
    }

    @Override
    public boolean isEmpty(int row, int col) {
        return board.isEmpty(row, col);
    }

    @Override
    public List<int[]> legalMoves() {
        return board.legalMoves();
    }

    @Override
    public boolean isTerminal() {
        return board.isTerminal();
    }

    @Override
    public boolean redWins() {
        return board.redWins();
    }

    @Override
    public boolean blackWins() {
        return board.blackWins();
    }

    @Override
    public AIBoardAdapter copy() {
        // Fast copy of board state; matrix rebuilt from board.
        Board b2 = board.fastCopy();
        b2.clearMoveHistory(); // no need to keep snapshots in copies
        return new BoardAdapter(b2);
    }

    @Override
    public boolean makeMove(int row, int col, Player player) {
        if (!Rules.validMove(board, row, col)) return false;

        // Update board
        if (player == Player.RED) {
            board.getMoveRed(row, col, null);
            matrix[row][col] = 1;
        } else {
            board.getMoveBlack(row, col, null);
            matrix[row][col] = 2;
        }
        return true;
    }

    @Override
    public void getMoveRed(int row, int col, Color colorIgnored) {
        board.getMoveRed(row, col, null);
        matrix[row][col] = 1;
    }

    @Override
    public void getMoveBlack(int row, int col, Color colorIgnored) {
        board.getMoveBlack(row, col, null);
        matrix[row][col] = 2;
    }

    @Override
    public List<int[]> neighbors(int row, int col) {
        return board.neighbors(row, col);
    }

    // ---------------- extra helpers you already had ----------------

    public int[][] getMatrix() {
        return matrix;
    }

    public Board getBoard() {
        return board;
    }

    public void updateMatrix() {
        int n = board.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                matrix[r][c] = colorToInt(board.getCell(r, c));
            }
        }
    }

    public void updateBoard() {
        board.reset();
        // rebuild by applying matrix stones
        int n = board.getSize();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int v = matrix[r][c];
                if (v == 1) board.getMoveRed(r, c, null);
                else if (v == 2) board.getMoveBlack(r, c, null);
            }
        }
        // important: reconstruction should not accumulate undo snapshots forever
        board.clearMoveHistory();
    }

    public void undoMove(int row, int col) {
        if (!inBounds(row, col)) return;
        matrix[row][col] = 0;
        updateBoard();
    }

    public boolean isGameOver() {
        return board.isTerminal();
    }

    public Player getWinner() {
        if (board.redWins()) return Player.RED;
        if (board.blackWins()) return Player.BLACK;
        return null;
    }

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

    private int colorToInt(Color color) {
        return switch (color) {
            case RED -> 1;
            case BLACK -> 2;
            case EMPTY -> 0;
        };
    }
}
