package AI.AiPlayer;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.MCTS;
import AI.mcts.Node;
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
import Game.Board;
import Game.Player;

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
