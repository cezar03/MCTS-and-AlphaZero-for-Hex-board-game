package UI;

import Game.BoardAdapter;
import Game.Player;

public class GameController {
    private BoardView boardView;
    private Player currentPlayer;
    private boolean gameOver;
    private BoardAdapter adapter;
    private final Player RED = Player.RED;
    private final Player BLACK = Player.BLACK;
    /*
     * Constructor for GameController
     */
    public GameController(BoardAdapter adapter0,BoardView boardView) {
        this.boardView = boardView;
        this.currentPlayer = RED; // can switch to BLACK to let black start
        this.gameOver = false;
        this.adapter = adapter0; // Initialize the adapter
        boardView.setController(this);
    }

    /** Handle a cell click at the given row, col from the BoardView. 
     * @param row The row of the clicked cell.
     * @param col The column of the clicked cell.
    */
    public void handleCellClick(int row, int col) {
        if (gameOver) {
            return;
        }
        // Make the move on the board
        boolean ok = adapter.makeMove(row, col, currentPlayer);
        if (!ok) {
            return;
        }

        // Update the BoardView to reflect the move
        // This might be wrong - check
        boardView.update(adapter);
        // Check for a win condition
        if (adapter.isGameOver()) {
            gameOver = true;
            // Determine winner
            String winText;
            switch (adapter.getWinner()) {
                case RED:
                    winText = "RED wins!";
                    break;
                case BLACK:
                    winText = "BLACK wins!";
                    break;
                default:
                    winText = "It's a draw!";
            }
            System.out.println(winText);
        }

        // No win yet, switch players
        currentPlayer = currentPlayer.other();
        // For debugging: print current player which how we could pop a dialog
        System.out.println("Current player: " + currentPlayer);
    }
}
