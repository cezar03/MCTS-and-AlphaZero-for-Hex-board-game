package AI.alphazero.player;

import AI.alphazero.config.AlphaZeroConfig;
import AI.alphazero.mcts.AlphaZeroMCTS;
import AI.api.AIAgent;
import AI.api.AIBoardAdapter;
import AI.mcts.Node;
import bridge.BoardConverters;
import game.core.Board;
import game.core.Color;
import game.core.Move;
import game.core.Player;

/**
 * An AI agent powered by the AlphaZero algorithm.
 * <p>
 * This agent uses a deep neural network combined with Monte Carlo Tree Search (MCTS)
 * to evaluate positions and select moves. It does not rely on hand-crafted heuristics
 * but rather on a learned policy and value function.
 */
public class AlphaZeroPlayer implements AIAgent {
    private final Player player;
    private final AlphaZeroMCTS mcts;
    private final AlphaZeroConfig config;

    /**
     * Constructs an AlphaZero player.
     * * @param player the player this agent controls
     * @param mcts the AlphaZero-specific MCTS implementation
     * @param config the configuration settings
     * @throws IllegalArgumentException if any argument is null
     */
    public AlphaZeroPlayer(Player player, AlphaZeroMCTS mcts, AlphaZeroConfig config) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (mcts == null) throw new IllegalArgumentException("MCTS cannot be null");
        if (config == null) throw new IllegalArgumentException("Config cannot be null");
        this.player = player;
        this.mcts = mcts;
        this.config = config;
    }

    /**
     * Determines the best move using the AlphaZero MCTS search.
     * <p>
     * The search is guided by the neural network's policy and value outputs.
     * In this implementation, exploration noise is disabled (deterministic play)
     * as this method is intended for gameplay, not training.
     * * @param boardAdapter the current board state
     * @param currentPlayer the player whose turn it is
     * @return the best {@link Move} found
     */
    @Override
    public Move getBestMove(AIBoardAdapter boardAdapter, Player currentPlayer) {
        if (boardAdapter == null) throw new IllegalArgumentException("Board cannot be null");
        if (currentPlayer == null) throw new IllegalArgumentException("Current player cannot be null");
        if (!controlsPlayer(currentPlayer)) {
            throw new IllegalArgumentException("This agent does not control player: " + currentPlayer);
        }

        Color color = currentPlayer.stone;

        Board gameBoard = BoardConverters.toBoard(boardAdapter);


        // IMPORTANT: UI play => training=false (no Dirichlet noise)
        Node root = mcts.search(gameBoard, color, config.getMctsIterations(), false);

        double[] policy = mcts.getSearchPolicy(root, config.getTemperature());

        return selectBestLegalMove(policy, boardAdapter);
    }

    /**
     * Returns the player controlled by this agent.
     * * @return the Player enum
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * Checks if the agent controls the specified player.
     * * @param p the player to check
     * @return true if IDs match
     */
    @Override
    public boolean controlsPlayer(Player p) {
        return this.player == p;
    }

    /**
     * Lifecycle method for initialization.
     * Currently a no-op for this implementation.
     */
    @Override
    public void initialize() {}

    /**
     * Lifecycle method for cleanup.
     * Currently a no-op for this implementation.
     */
    @Override
    public void cleanup() {}

    /**
     * Selects the best legal move based on the provided policy probabilities.
     * * @param policy the array of move probabilities from the neural network
     * @param boardAdapter the current board state to check legality
     * @return the best legal {@link Move}, or null if no legal moves are available
     */
    private Move selectBestLegalMove(double[] policy, AIBoardAdapter boardAdapter) {
        int n = boardAdapter.getSize();

        double best = Double.NEGATIVE_INFINITY;
        int bestR = -1, bestC = -1;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (boardAdapter.getCell(r, c) != Color.EMPTY) continue;
                int idx = r * n + c;
                if (idx >= 0 && idx < policy.length && policy[idx] > best) {
                    best = policy[idx];
                    bestR = r;
                    bestC = c;
                }
            }
        }

        if (bestR == -1) return null; // no legal moves
        return Move.get(bestR, bestC);
    }
}











