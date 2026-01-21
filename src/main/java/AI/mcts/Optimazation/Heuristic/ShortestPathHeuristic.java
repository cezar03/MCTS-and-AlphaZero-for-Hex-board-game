package AI.mcts.Optimazation.Heuristic;

import AI.mcts.HexGame.GameState;
import game.core.Move;

/**
 * A heuristic that evaluates moves based on their impact on the shortest path
 * to winning for the current player.
 * <p>
 * This heuristic compares the estimated shortest path length before and after
 * making a move. Moves that reduce the shortest path length are scored higher,
 * as they represent progress toward connecting the player's winning edges.
 * The score is normalized using a hyperbolic tangent function to bound the
 * output in a reasonable range.
 * <p>
 * Note: This heuristic is computationally more expensive than simpler heuristics
 * as it requires creating a copy of the game state and computing shortest paths.
 */
public final class ShortestPathHeuristic implements Heuristic {

    /**
     * Scores a move based on how much it reduces the shortest path to winning.
     * The score is calculated as tanh(delta/3.0), where delta is the reduction
     * in shortest path length (before - after). Positive deltas (path reduction)
     * yield positive scores, while negative deltas yield negative scores.
     * 
     * @param state the current game state before the move
     * @param move the move to evaluate
     * @return a score roughly in the range [-1, 1], where positive values indicate
     *         the move reduces the shortest path and negative values indicate it
     *         increases the shortest path
     */
    @Override
    public double score(GameState state, Move move) {
        int before = state.estimateShortestPathForCurrentPlayer();
        GameState copy = state.copy();
        copy.doMove(move);
        int after = copy.estimateShortestPathForCurrentPlayer();
        int delta = before - after;
        return Math.tanh(delta / 3.0);
    }
}











