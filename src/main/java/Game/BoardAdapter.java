package Game;

public class BoardAdapter {
    private final Board board;
    private final int[][] matrix;

    public BoardAdapter(Board board) {
        this.board = board;
        this.matrix = new int[board.getsize()][board.getsize()];
        updateMatrix();
    }



    //Updates the 2D matrix  based on the current state of the board
    public void updateMatrix() {
        int n = board.getsize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                Color cellColor = board.getcell(row, col);
                matrix[row][col] = colorToInt(cellColor);
            }
        }
    }

    //Updates the board within the changes in the matrix
    public void updateBoard() {
        int n = board.getsize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                Color currentColor = board.getcell(row, col);
                Color matrixColor = intToColor(matrix[row][col]);

                // Only update if smth is different
                if (currentColor != matrixColor) {
                    if (matrixColor == Color.RED) {
                        board.getmovered(row, col, Color.RED);
                    } else if (matrixColor == Color.BLACK) {
                        board.getmoveblack(row, col, Color.BLACK);
                    } else if (matrixColor == Color.EMPTY) {
                        board.clearCell(row, col);
                    }
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
     //Where 0 = empty, 1 = red, 2 = bleck

    private int colorToInt(Color color) {
        switch (color) {
            case RED: return 1;
            case BLACK: return 2;
            case EMPTY:
            default: return 0;
        }
    }


    private Color intToColor(int value) {
        switch (value) {
            case 1: return Color.RED;
            case 2: return Color.BLACK;
            case 0:
            default: return Color.EMPTY;
        }
    }

    public boolean makeMove(int row, int col, Player player) {
        if (!Rules.validMove(board, row, col)) {
            return false;
        }

        // Update matrix
        matrix[row][col] = player.id;

        // Update board
        if (player == Player.RED) {
            board.getmovered(row, col, Color.RED);
        } else {
            board.getmoveblack(row, col, Color.BLACK);
        }

        return true;
    }


    public void undoMove(int row, int col) {
        matrix[row][col] = 0;
        board.clearCell(row, col);
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
        int n = board.getsize();
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                matrix[row][col] = 0;
                board.clearCell(row, col);
            }
        }
    }
}
