package AI.mcts.Optimazation.Heuristic;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import Game.Board;

/**
 * A heuristic that favors moves closer to the center of the board.
 * <p>
 * This heuristic is based on the strategic principle that central positions
 * often provide better connectivity options and greater control of the board.
 * The score decreases linearly with distance from the center, normalized to
 * a range of [0, 1], where 1 represents the center position and 0 represents
 * the farthest corner.
 */
public final class CentralityHeuristic  implements Heuristic{

    /**
     * Scores a move based on its proximity to the board's center.
     * The score is calculated as 1 minus the normalized distance from the center,
     * where the center is defined as ((n-1)/2, (n-1)/2) for an n×n board.
     * 
     * @param state the current game state containing the board
     * @param move the move to be scored based on its position
     * @return a score in the range [0, 1], where 1 indicates the center position
     *         and lower values indicate positions farther from the center
     */
    @Override
    public double score(GameState state, Move move) {
        Board board = state.getBoard();
        int n = board.getSize();
        double centerRow = (n - 1) / 2.0;
        double centerCol = (n - 1) / 2.0;
        double distCenter = Math.hypot(move.row - centerRow, move.col - centerCol);
        double maxDist = Math.hypot(centerRow, centerCol);
        return 1.0 - distCenter / (maxDist + 1e-9);
    }
}
