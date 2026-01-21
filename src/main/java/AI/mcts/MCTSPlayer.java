package AI.mcts;

import AI.api.AIAgent;
import AI.api.AIBoardAdapter;
import AI.mcts.HexGame.GameState;
import AI.mcts.Optimazation.Heuristic.CentralityHeuristic;
import AI.mcts.Optimazation.Heuristic.ConnectivityHeuristic;
import AI.mcts.Optimazation.Heuristic.Heuristic;
import AI.mcts.Optimazation.Heuristic.LinearCombinationHeuristic;
import AI.mcts.Optimazation.Heuristic.ShortestPathHeuristic;
import AI.mcts.Optimazation.MovePruner;
import AI.mcts.Steps.Expansion;
import AI.mcts.Steps.Selection;
import AI.mcts.Steps.SimulationStep.BaseSimulation;
import AI.mcts.Steps.SimulationStep.Simulation;
import bridge.BoardConverters;
import game.core.Board;
import game.core.Move;
import game.core.Player;

/**
 * An AI agent implementation based on Monte Carlo Tree Search (MCTS).
 * <p>
 * This player can be configured in two modes:
 * <ul>
 * <li>Basic MCTS: Uses standard UCT selection and random rollouts.</li>
 * <li>Optimized MCTS: Incorporates domain-specific heuristics (Centrality, Connectivity, Shortest Path)
 * and move pruning to guide the search and improve performance.</li>
 * </ul>
 */
public class MCTSPlayer implements AIAgent {
    private final MCTS mcts;
    private final Player mctsPlayer;
    private final int iterations;

    private final double threshold;
    private final double centralityWeight;
    private final double connectivityWeight;

    /**
     * Constructs a basic MCTS player without advanced heuristics or pruning.
     * * @param mctsPlayer the player this agent controls
     * @param iterations the number of simulations to run per move
     */
    public MCTSPlayer(Player mctsPlayer, int iterations) {
        this.mctsPlayer = mctsPlayer;
        this.iterations = iterations;

        this.threshold = 0.0;
        this.centralityWeight = 0.0;
        this.connectivityWeight = 0.0;

        Selection selection = new Selection(Math.sqrt(2));
        Expansion expansion = new Expansion(null, null, 0.0);
        Simulation simulation = new BaseSimulation();

        this.mcts = new MCTS(iterations, selection, expansion, simulation);
    }

    /**
     * Constructs an optimized MCTS player with configurable heuristics and pruning.
     * * @param mctsPlayer the player this agent controls
     * @param iterations the number of simulations to run per move
     * @param threshold the pruning threshold (moves below max_score - threshold are discarded)
     * @param centralityWeight weight for the centrality heuristic
     * @param connectivityWeight weight for the connectivity heuristic
     * @param biasScale scaling factor for the heuristic bias in expansion
     * @param spWeight weight for the shortest path heuristic
     * @param cExploration the exploration constant (C) for UCT
     */
    public MCTSPlayer(Player mctsPlayer, int iterations, double threshold, double centralityWeight,
                      double connectivityWeight, double biasScale, double spWeight, double cExploration) {

        this.mctsPlayer = mctsPlayer;
        this.iterations = iterations;
        this.threshold = threshold;
        this.centralityWeight = centralityWeight;
        this.connectivityWeight = connectivityWeight;

        Heuristic centrality = new CentralityHeuristic();
        Heuristic connectivity = new ConnectivityHeuristic();
        Heuristic sp = new ShortestPathHeuristic();

        Heuristic combined = new LinearCombinationHeuristic(
                centrality, connectivity, sp,
                centralityWeight, connectivityWeight, spWeight
        );

        int minMoves = 4;
        MovePruner pruner = new MovePruner(threshold, minMoves, combined);

        Selection selection = new Selection(cExploration);
        Expansion expansion = new Expansion(pruner, combined, biasScale);
        Simulation simulation = new BaseSimulation();

        this.mcts = new MCTS(iterations, selection, expansion, simulation);
    }

    /**
     * Executes the MCTS search to determine the best move for the current position.
     * <p>
     * Converts the abstract board adapter to a concrete Board, initializes the
     * MCTS tree, runs the specified number of iterations, and returns the move
     * corresponding to the most visited child of the root.
     * * @param boardAdapter the current game board
     * @param currentPlayer the player whose turn it is
     * @return the calculated best Move, or a fallback legal move if search fails
     */
    @Override
    public Move getBestMove(AIBoardAdapter boardAdapter, Player currentPlayer) {
        Board board = BoardConverters.toBoard(boardAdapter);
        GameState gameState = new GameState(board, currentPlayer);
        if (gameState.getLegalMoves().isEmpty()) return null;
        GameState simState = gameState.copy();
        Node root = new Node(null, null, currentPlayer.other().id);
        Node bestNode = mcts.search(root, simState);
        if (bestNode == null || bestNode.move == null) { return gameState.getLegalMoves().get(0);}
        return bestNode.move;
    }

<<<<<<< HEAD
    /**
     * Returns the player controlled by this MCTS agent.
     * * @return the Player enum
     */
    @Override
    public Player getPlayer() { return mctsPlayer; }

    /**
     * Verifies if this agent is responsible for the given player.
     * * @param player the player to check
     * @return true if it matches the agent's player
     */
    @Override
    public boolean controlsPlayer(Player player) { return this.mctsPlayer == player; }
=======
    @Override public Player getPlayer() { return mctsPlayer; }
    @Override public boolean controlsPlayer(Player player) { return this.mctsPlayer == player; }
>>>>>>> 1165bedc5af5867e936278ee2626c1ff7663bbd5

    /**
     * Gets the number of iterations configured for this agent.
     * * @return the iteration count
     */
    public int getIterations() { return iterations; }

    /**
     * Retrieves the MovePruner instance used by the internal MCTS.
     * * @return the MovePruner, or null if not configured
     */
    public MovePruner getPruner() { return mcts.getPruner(); }

    /**
     * Gets the configured pruning threshold.
     * * @return the threshold value
     */
    public double getThreshold() { return threshold; }

    /**
     * Gets the configured weight for centrality heuristic.
     * * @return the weight
     */
    public double getCentralityWeight() { return centralityWeight; }

    /**
     * Gets the configured weight for connectivity heuristic.
     * * @return the weight
     */
    public double getConnectivityWeight() { return connectivityWeight; }
}











