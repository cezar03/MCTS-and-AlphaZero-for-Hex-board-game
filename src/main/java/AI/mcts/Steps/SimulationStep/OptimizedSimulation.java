package ai.mcts.Steps.SimulationStep;

import java.util.*;
import ai.mcts.HexGame.GameState;
import game.core.Move;
import ai.mcts.Optimazation.MovePruner;

/**
 * Implements an optimized simulation strategy for Monte Carlo Tree Search
 * that uses move pruning and heuristic evaluation to guide simulations.
 * 
 * <p>This strategy uses an epsilon-greedy approach: with probability epsilon,
 * a random move is selected; otherwise, the move with the best heuristic score
 * is chosen. Move pruning can further reduce the branching factor during simulation.</p>
 */
public class OptimizedSimulation implements Simulation {
    private final Random random = new Random();
    private final MovePruner pruner;
    private final double epsilon;

    /**
     * Constructs an OptimizedSimulation with the specified pruner and epsilon value.
     *
     * @param pruner the MovePruner to filter moves during simulation (may be null)
     * @param epsilon the probability of selecting a random move (0.0 to 1.0)
     */
    public OptimizedSimulation(MovePruner pruner, double epsilon) {
        this.pruner = pruner;
        this.epsilon = epsilon;
    }

    /**
     * Simulates a game from the given starting state using pruned moves and
     * heuristic guidance. The simulation is performed on a copy of the state
     * to avoid modifying the original.
     *
     * @param start the game state from which to begin simulation
     * @return the ID of the winning player (0 for no winner, 1 for RED, 2 for BLACK)
     */
    @Override
    public int simulate(GameState start) {
        GameState state = start.copy();
        while (!state.isTerminal()) {
            List<Move> legal = state.getLegalMoves();
            if (legal.isEmpty()) break;

            List<Move> moves = legal;
            if (pruner != null) {
                List<Move> pruned = pruner.pruneMoves(state, legal);
                if (!pruned.isEmpty()) moves = pruned;
            }

            Move chosen = chooseMove(state, moves);
            state.doMove(chosen);
        }
        return state.getWinnerId();
    }

    /**
     * Chooses a move using an epsilon-greedy strategy. With probability epsilon,
     * a random move is selected; otherwise, the move with the best heuristic
     * evaluation is chosen.
     *
     * @param state the current game state
     * @param legal the list of legal moves to choose from
     * @return the selected move
     */
    private Move chooseMove(GameState state, List<Move> legal) {
        if (random.nextDouble() < epsilon) {
            return legal.get(random.nextInt(legal.size()));
        }
        return bestHeuristicMove(state, legal);
    }

    /**
     * Selects the move with the best (lowest) heuristic score from the legal moves.
     * The heuristic is based on the shortest path distance after making each move.
     *
     * @param state the current game state
     * @param legal the list of legal moves to evaluate
     * @return the move with the best heuristic score
     */
    private Move bestHeuristicMove(GameState state, List<Move> legal) {
        Move best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Move move : legal) {
            int score = state.estimateAfterMove(move);
            if (score < bestScore) {
                bestScore = score;
                best = move;
            }
        }
        return best;
    }
}











