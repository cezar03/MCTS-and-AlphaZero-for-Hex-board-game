package ai.mcts.Optimazation.Heuristic;

import ai.mcts.HexGame.GameState;
import game.core.Move;

/**
 * A composite heuristic that combines multiple individual heuristics using
 * weighted linear combination.
 * <p>
 * This heuristic allows balancing different strategic considerations by
 * assigning weights to centrality, connectivity, and shortest path heuristics.
 * The final score is computed as:
 * <pre>
 * score = wCenter * centrality + wConn * connectivity + wSP * shortestPath
 * </pre>
 * <p>
 * The shortest path component is optional and can be set to null. The shortest
 * path weight (wSP) should typically be kept small (e.g., 0.1) as shortest path
 * computation is more expensive and can dominate the other components.
 */
public final class LinearCombinationHeuristic implements Heuristic {

    private final Heuristic centrality;
    private final Heuristic connectivity;
    private final Heuristic shortestPath; // can be null
    private final double wCenter;
    private final double wConn;
    private final double wSP;     // keep small (e.g. 0.1)

    /**
     * Creates a new linear combination heuristic with the specified components
     * and weights.
     * 
     * @param centrality the heuristic for evaluating move centrality
     * @param connectivity the heuristic for evaluating move connectivity
     * @param shortestPath the heuristic for evaluating shortest path impact, or null to disable
     * @param wCenter the weight for the centrality component
     * @param wConn the weight for the connectivity component
     * @param wSP the weight for the shortest path component (recommended to keep small, e.g., 0.1)
     */
    public LinearCombinationHeuristic(Heuristic centrality,
                                      Heuristic connectivity,
                                      Heuristic shortestPath,
                                      double wCenter,
                                      double wConn,
                                      double wSP) {
        this.centrality = centrality;
        this.connectivity = connectivity;
        this.shortestPath = shortestPath;
        this.wCenter = wCenter;
        this.wConn = wConn;
        this.wSP = wSP;
    }

    /**
     * Computes the weighted linear combination of all enabled heuristic components.
     * If the shortest path heuristic is null or its weight is 0, that component
     * is skipped.
     * 
     * @param state the current game state
     * @param move the move to evaluate
     * @return the weighted sum of all heuristic component scores
     */
    @Override
    public double score(GameState state, Move move) {
        double score = 0.0;
        score += wCenter * centrality.score(state, move);
        score += wConn   * connectivity.score(state, move);

        if (shortestPath != null && wSP != 0.0) {
            score += wSP * shortestPath.score(state, move);
        }

        return score;
    }
}











