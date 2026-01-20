package ai.mcts;

import ai.api.AIAgent;
import ai.api.AIBoardAdapter;
import ai.mcts.HexGame.GameState;
import ai.mcts.Optimazation.Heuristic.CentralityHeuristic;
import ai.mcts.Optimazation.Heuristic.ConnectivityHeuristic;
import ai.mcts.Optimazation.Heuristic.Heuristic;
import ai.mcts.Optimazation.Heuristic.LinearCombinationHeuristic;
import ai.mcts.Optimazation.Heuristic.ShortestPathHeuristic;
import ai.mcts.Optimazation.MovePruner;
import ai.mcts.Steps.Expansion;
import ai.mcts.Steps.Selection;
import ai.mcts.Steps.SimulationStep.BaseSimulation;
import ai.mcts.Steps.SimulationStep.Simulation;
import bridge.BoardConverters;
import game.core.Board;
import game.core.Move;
import game.core.Player;

public class MCTSPlayer implements AIAgent {
    private final MCTS mcts;
    private final Player mctsPlayer;
    private final int iterations;

    private final double threshold;
    private final double centralityWeight;
    private final double connectivityWeight;

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

    @Override
    public Move getBestMove(AIBoardAdapter boardAdapter, Player currentPlayer) {
        Board board = BoardConverters.toBoard(boardAdapter);

        GameState gameState = new GameState(board, currentPlayer);

        if (gameState.getLegalMoves().isEmpty()) return null;

        GameState simState = gameState.copy();
        Node root = new Node(null, null, currentPlayer.other().id);

        Node bestNode = mcts.search(root, simState);
        if (bestNode == null || bestNode.move == null) {
            return gameState.getLegalMoves().get(0);
        }
        return bestNode.move;
    }

    @Override
    public Player getPlayer() { return mctsPlayer; }

    @Override
    public boolean controlsPlayer(Player player) { return this.mctsPlayer == player; }

    public int getIterations() { return iterations; }
    public MovePruner getPruner() { return mcts.getPruner(); }
    public double getThreshold() { return threshold; }
    public double getCentralityWeight() { return centralityWeight; }
    public double getConnectivityWeight() { return connectivityWeight; }
}











