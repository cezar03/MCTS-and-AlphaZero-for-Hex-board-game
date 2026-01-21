package AI.mcts.Optimazation;

import AI.mcts.HexGame.GameState;
import game.core.Move;
import AI.mcts.Optimazation.Heuristic.*;

import java.util.*;

/**
 * A move pruning utility that reduces the branching factor in game tree search
 * by filtering out less promising moves based on heuristic evaluation.
 * <p>
 * The pruner works by scoring all legal moves using a heuristic function, then
 * keeping only moves whose scores are within a threshold of the maximum score.
 * This reduces the search space while retaining the most strategically valuable
 * moves. A minimum number of moves is always kept to prevent over-pruning.
 * <p>
 * This is particularly useful in MCTS (Monte Carlo Tree Search) to focus
 * computational resources on more promising moves.
 */
public class MovePruner {

    //set thershold to represent how many moves we want to keep (smaller=fewer)
    private final double threshold;

    // heuristics
    private final int minMoves;
    private final Heuristic heuristic;

    /**
     * Creates a new move pruner with the specified configuration.
     * This constructor allows different agents to use different pruning settings,
     * which is useful for testing and tuning.
     * 
     * @param threshold the score difference threshold; only moves with scores within
     *                  this range of the maximum score are kept (smaller values result
     *                  in more aggressive pruning)
     * @param minMoves the minimum number of moves to keep regardless of scores, to
     *                 prevent over-pruning
     * @param heuristic the heuristic function used to score moves
     */
    public MovePruner(double threshold, int minMoves, Heuristic heuristic) {
        this.threshold = threshold;
        this.minMoves = minMoves;
        this.heuristic = heuristic;
    }


    /**
     * Prunes a list of legal moves, keeping only the most promising ones based on
     * heuristic evaluation.
     * <p>
     * The pruning algorithm:
     * <ol>
     * <li>Scores all legal moves using the heuristic</li>
     * <li>Finds the maximum score</li>
     * <li>Keeps moves with scores >= (maxScore - threshold)</li>
     * <li>If fewer than minMoves remain, keeps the top minMoves by score instead</li>
     * </ol>
     * 
     * @param state the current game state
     * @param legalMoves the list of all legal moves to consider
     * @return a pruned list containing only the most promising moves, with at least
     *         minMoves entries (unless fewer legal moves exist)
     */
    public List<Move> pruneMoves(GameState state, List<Move> legalMoves) {
        if (legalMoves.isEmpty()) return legalMoves;

        Map<Move, Double> scores = new HashMap<>();
        double maxScore = Double.NEGATIVE_INFINITY;

        for (Move m : legalMoves) {
            double s = heuristic.score(state, m);
            scores.put(m, s);
            if (s > maxScore) maxScore = s;
        }

        double min = maxScore - threshold;
        List<Move> pruned = new ArrayList<>();
        for (Move m : legalMoves) {
            if (scores.get(m) >= min) {
                pruned.add(m);
            }
        }

        if (pruned.size() < minMoves) {
            legalMoves.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));
            pruned.clear();
            for (int i = 0; i < Math.min(minMoves, legalMoves.size()); i++) {
                pruned.add(legalMoves.get(i));
            }
        }
        return pruned;
    }

    /**
     * Returns the threshold value used by this pruner.
     * 
     * @return the score difference threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Returns the heuristic function used by this pruner.
     * 
     * @return the heuristic used for scoring moves
     */
    public Heuristic getHeuristic() {
        return heuristic;
    }
}











