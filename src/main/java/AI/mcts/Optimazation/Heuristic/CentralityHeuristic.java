package ai.mcts.Optimazation.Heuristic;

import ai.mcts.HexGame.GameState;
import game.core.Move;
import game.core.Board;

/**
 * A heuristic that favors moves closer to the center of the board.
 * <p>
 * This heuristic is based on the strategic principle that central positions
 * often provide better connectivity options and greater control of the board.
 * The score decreases linearly with distance from the center, normalized to
 * a range of [0, 1], where 1 represents the center position and 0 represents
 * the farthest corner.
 */
public final class CentralityHeuristic implements Heuristic {
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











