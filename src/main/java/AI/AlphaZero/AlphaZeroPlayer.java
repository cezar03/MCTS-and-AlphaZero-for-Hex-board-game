package AI.AlphaZero;

import AI.AiPlayer.AIBoardAdapter;
import AI.AiPlayer.AIAgent;
import AI.mcts.Node;
import AI.mcts.HexGame.Move;
import Game.Board;
import Game.Color;
import Game.Player;

/**
 * AlphaZero AI agent implementation.
 * Wraps AlphaZeroMCTS and AlphaZeroNet to provide an AIAgent interface.
 * Uses Monte Carlo Tree Search with neural network evaluation for move selection.
 * 
 * @author Team 04
 */
public class AlphaZeroPlayer implements AIAgent {
    private final Player player;
    private final AlphaZeroMCTS mcts;
    private final AlphaZeroConfig config;
    
    public AlphaZeroPlayer(Player player, AlphaZeroMCTS mcts, AlphaZeroConfig config) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (mcts == null) {
            throw new IllegalArgumentException("MCTS cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.player = player;
        this.mcts = mcts;
        this.config = config;
    }
    
    @Override
    public Move getBestMove(AIBoardAdapter boardAdapter, Player currentPlayer) {
        if (boardAdapter == null) {
            throw new IllegalArgumentException("Board cannot be null");
        }
        if (currentPlayer == null) {
            throw new IllegalArgumentException("Current player cannot be null");
        }
        
        if (!controlsPlayer(currentPlayer)) {
            throw new IllegalArgumentException("This agent does not control player: " + currentPlayer);
        }
        
        // Determine the color based on the player
        Color color = playerToColor(currentPlayer);
        
        // Create a board from the adapter for MCTS
        Board gameBoard = createBoardFromAdapter(boardAdapter);
        
        // Run MCTS search
        Node root = mcts.search(gameBoard, color, config.getMctsIterations());
        
        // Get policy based on temperature setting
        double[] policy = mcts.getSearchPolicy(root, config.getTemperature(), config.getBoardSize());
        
        // Select best move from policy
        return selectBestMoveFromPolicy(policy, boardAdapter, config.getBoardSize());
    }
    
    @Override
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public boolean controlsPlayer(Player player) {
        return this.player.equals(player);
    }
    
    @Override
    public void initialize() {
        // Optional: Perform any initialization specific to AlphaZero
        // For example, loading a pre-trained model if needed
    }
    
    @Override
    public void cleanup() {
        // Optional: Perform any cleanup specific to AlphaZero
        // For example, releasing model resources
    }
    
    /**
     * Converts a Player to a Color.
     * @param player The player to convert
     * @return The corresponding Color (RED or BLACK)
     */
    private Color playerToColor(Player player) {
        return player.stone;
    }
    
    /**
     * Creates a Board from an AIBoardAdapter.
     * This is a simplified conversion that may need refinement based on your Board implementation.
     * @param adapter The board adapter
     * @return A new Board with the current state
     */
    private Board createBoardFromAdapter(AIBoardAdapter adapter) {
        Board board = new Board(adapter.getSize());
        
        // Recreate the board state from the adapter
        for (int row = 0; row < adapter.getSize(); row++) {
            for (int col = 0; col < adapter.getSize(); col++) {
                Color cellColor = adapter.getCell(row, col);
                if (cellColor == Color.RED) {
                    board.getMoveRed(row, col, Color.RED);
                } else if (cellColor == Color.BLACK) {
                    board.getMoveBlack(row, col, Color.BLACK);
                }
            }
        }
        
        return board;
    }
    
    /**
     * Selects the best move from the policy distribution.
     * @param policy The move probability distribution
     * @param boardAdapter The current game board adapter
     * @param boardSize The size of the board
     * @return The selected move
     */
    private Move selectBestMoveFromPolicy(double[] policy, AIBoardAdapter boardAdapter, int boardSize) {
        // Find the move with the highest probability that is also legal
        double maxProb = -1;
        Move bestMove = null;
        
        for (int[] legalMove : boardAdapter.legalMoves()) {
            int idx = legalMove[0] * boardSize + legalMove[1];
            if (idx >= 0 && idx < policy.length && policy[idx] > maxProb) {
                maxProb = policy[idx];
                bestMove = new Move(legalMove[0], legalMove[1]);
            }
        }
        
        if (bestMove == null) {
            // Fallback: pick first legal move if no move found
            java.util.List<int[]> legalMoves = boardAdapter.legalMoves();
            if (!legalMoves.isEmpty()) {
                int[] move = legalMoves.get(0);
                bestMove = new Move(move[0], move[1]);
            } else {
                throw new IllegalStateException("No legal move found");
            }
        }
        
        return bestMove;
    }
}
