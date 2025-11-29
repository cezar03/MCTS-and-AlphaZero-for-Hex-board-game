package AI.mcts.Steps.SimulationStep;

import java.util.*;
import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.MovePruner;

public class OptimizedSimulation implements Simulation {
    private final Random random = new Random();
    private final MovePruner pruner;
    private final double epsilon;

    public OptimizedSimulation(MovePruner pruner, double epsilon) {
        this.pruner = pruner;
        this.epsilon = epsilon;
    }

    @Override
    public int simulate(GameState start) {
        GameState state = start.copy();
        while (!state.isTerminal()) {
            List<Move> legal = state.getLegalMoves();
            if (legal.isEmpty()) break;

            List<Move> pruned = pruner.pruneMoves(state, legal);
            if (pruned.isEmpty()) pruned = legal;

            Move chosen = chooseMove(state, pruned);
            state.doMove(chosen);
        }
        return state.getWinnerId();
    }

    private Move chooseMove(GameState state, List<Move> legal) {
        if (random.nextDouble() < epsilon) {
            return legal.get(random.nextInt(legal.size()));
        }
        return bestHeuristicMove(state, legal);
    }

    private Move bestHeuristicMove(GameState state, List<Move> legal) {
        Move best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Move m : legal) {
            int score = state.estimateAfterMove(m);  // uses ShortestPath internally
            if (score < bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best;
    }
}
