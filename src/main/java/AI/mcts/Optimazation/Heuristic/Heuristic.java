package AI.mcts.Optimazation.Heuristic;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;

/**
 * A heuristic interface for evaluating the quality of moves in a game state.
 * Heuristics provide strategic guidance by scoring potential moves, allowing
 * AI agents to prioritize more promising moves during search.
 * <p>
 * Implementations of this interface should return higher scores for moves that are strategically
 * better according to their specific evaluation criteria.
 */
public interface Heuristic {
    /**
     * Evaluates and scores a potential move in the given game state.
     * 
     * @param state the current game state in which the move is being evaluated
     * @param move the move to be scored
     * @return a score representing the quality of the move, where higher values
     *         indicate better moves according to this heuristic's criteria
     */
    double score(GameState state, Move move);
}
