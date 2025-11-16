package UI;

import Game.BoardAdapter;
import Game.Color;
import Game.Player;
import Game.Rules;
import AI.AIPlayer;
import AI.mcts.HexGame.Move;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.swing.Timer;

/** Controller class to manage game logic and interactions between the BoardAdapter and BoardView 
 * @author Team 04
*/
public class GameController {
    private BoardView boardView;
    private Player currentPlayer;
    private boolean gameOver;
    private BoardAdapter adapter;
    private int moveCount = 0;
    
    private AIPlayer aiPlayer;
    private boolean aiThinking = false;
    /*
     * Constructor for GameController
     * @param adapter0 The BoardAdapter to interact with the game board.
     * @param boardView The BoardView to update the UI.
     */
    public GameController(BoardAdapter adapter0, BoardView boardView) {
        this.boardView = boardView;
        this.currentPlayer = Player.RED; // can switch to Player.BLACK to let black start
        this.gameOver = false;
        this.adapter = adapter0; // Initialize the adapter
        this.aiPlayer = null;
        boardView.setController(this);
        boardView.updateTurnDisplay(currentPlayer);
    }

    /**
     * Returns the current player whose turn it is to make a move.
     * 
     * @return the current player (either RED or BLACK)
    */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /** Handle a cell click at the given row, col from the BoardView. 
     * @param row The row of the clicked cell.
     * @param col The column of the clicked cell.
    */
    public void handleCellClick(int row, int col) {
        if (gameOver || aiThinking) {
            return;
        }
        
        // Check if AI should make a move
        if (aiPlayer != null && aiPlayer.controlsPlayer(currentPlayer)) {
            return;
        }
        
        // Check if pie rule is available (second player's first move)
        if (Rules.pieRuleAvailable(moveCount, currentPlayer)) {
            // Check if clicking on opponent's stone
            Color clickedCell = adapter.getCellColor(row, col);
            if (clickedCell == currentPlayer.other().stone) {
                adapter.undoMove(row, col); 
                adapter.makeMove(row, col, currentPlayer);
                boardView.update(adapter);
 
                moveCount++;
                System.out.println(moveCount);
                currentPlayer = currentPlayer.other();
                boardView.updateTurnDisplay(currentPlayer);

                if (aiPlayer != null && aiPlayer.controlsPlayer(currentPlayer)) {
                    makeAIMove();
                }
                return;
            }
        }
        
        boolean ok = adapter.makeMove(row, col, currentPlayer);
        if (!ok) {
            return;
        }
        // Update the BoardView to reflect the move
        boardView.update(adapter);
        moveCount++;

        if (adapter.isGameOver()) {
            gameOver = true;
            String winText;
            Player winner = adapter.getWinner();
            switch (winner) {
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
            boardView.updateWinDisplay(winner);
            return;
        }

        // No win yet, switch players
        currentPlayer = currentPlayer.other();
        boardView.updateTurnDisplay(currentPlayer);
        System.out.println("Current player: " + currentPlayer);
        
        // Check if AI should move
        if (aiPlayer != null && aiPlayer.controlsPlayer(currentPlayer)) {
            makeAIMove();
        }
    }

    /**
     * Sets up an AI player for the specified player type.
     * @param player The player to control with AI (RED or BLACK)
     * @param iterations The number of MCTS iterations to perform
     */
    public void setupAIPlayer(Player player, int iterations) {
        this.aiPlayer = new AIPlayer(player, iterations);
        System.out.println("AI Player set up for: " + player + " with " + iterations + " iterations");
        
        if (aiPlayer.controlsPlayer(currentPlayer) && !gameOver) {
            makeAIMove();
        }
    }

    /**
     * Removes the AI player from the game (returns to player vs player).
     */
    public void removeAIPlayer() {
        this.aiPlayer = null;
        this.aiThinking = false;
        System.out.println("AI Player removed");
    }

    /**
     * Makes an AI move on a background thread to avoid blocking the UI.
     */
    private void makeAIMove() {
        if (aiThinking || gameOver) {
            return;
        }

        aiThinking = true;
        
        // Run AI move calculation on a background thread
        Task<Move> aiTask = new Task<Move>() {
            @Override
            protected Move call() throws Exception {
                // Get the best move from the AI
                return aiPlayer.getBestMove(adapter.getBoard(), currentPlayer);
            }
        };

        aiTask.setOnSucceeded(event -> {
            Move bestMove = aiTask.getValue();
            if (bestMove != null) {
                Platform.runLater(() -> {
                    handleAIMove(bestMove.row, bestMove.col);
                });
            }
            aiThinking = false;
        });

        aiTask.setOnFailed(event -> {
            System.out.println("AI move calculation failed: " + aiTask.getException());
            aiThinking = false;
        });

        Thread aiThread = new Thread(aiTask);
        aiThread.setDaemon(true);
        aiThread.start();
    }

    /**
     * Handles the actual placement of the AI's move on the board.
     * @param row The row of the move
     * @param col The column of the move
     */
    private void handleAIMove(int row, int col) {
        System.out.println("AI makes move at: (" + row + ", " + col + ")");
        
        // Make the move
        boolean ok = adapter.makeMove(row, col, currentPlayer);
        if (!ok) {
            System.out.println("AI move failed!");
            aiThinking = false;
            return;
        }

        // Update the BoardView
        boardView.update(adapter);
        moveCount++;

        // Check for a win condition
        if (adapter.isGameOver()) {
            gameOver = true;
            Player winner = adapter.getWinner();
            String winText;
            switch (winner) {
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
            boardView.updateWinDisplay(winner);
            return;
        }

        // Switch to the other player
        currentPlayer = currentPlayer.other();
        boardView.updateTurnDisplay(currentPlayer);

        // If the next player is also AI, continue
        if (aiPlayer != null && aiPlayer.controlsPlayer(currentPlayer)) {
            // Add a small delay to make the game more watchable
            Timer timer = new Timer(500, e -> makeAIMove());
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * Checks if an AI player is currently active.
     * @return true if an AI player is set up, false otherwise
     */
    public boolean hasAIPlayer() {
        return aiPlayer != null;
    }

    /**
     * Gets the current AI player.
     * @return The AIPlayer instance, or null if no AI is set up
     */
    public AIPlayer getAIPlayer() {
        return aiPlayer;
    }
}