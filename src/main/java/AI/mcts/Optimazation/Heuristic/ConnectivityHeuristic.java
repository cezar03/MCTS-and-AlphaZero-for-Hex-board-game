package AI.mcts.Optimazation.Heuristic;

import AI.mcts.HexGame.GameState;
import game.core.Move;
import game.core.Board;
import game.core.Color;
import game.core.Player;

/**
 * A heuristic that favors moves adjacent to the current player's existing stones.
 * <p>
 * This heuristic evaluates connectivity by calculating the ratio of friendly
 * neighboring cells to total neighboring cells. Moves with more friendly neighbors
 * receive higher scores, promoting the formation of connected groups and paths.
 * This is particularly valuable in connection games like Hex where building
 * connected paths is the primary objective.
 */
public final class ConnectivityHeuristic implements Heuristic {
    
    /**
     * Scores a move based on the proportion of adjacent friendly stones.
     * * @param state the current game state
     * @param move the move to evaluate
     * @return a score between 0 and 1, with higher scores for moves with
     */
    @Override
    public double score(GameState state, Move move) {
        Board board = state.getBoard();
        Player toMove = state.getToMove();
        Color myColor = (toMove == Player.RED ? Color.RED : Color.BLACK);

        int friendly = 0;
        int total = 0;

        for (int[] rc : board.neighbors(move.row, move.col)) {
            total++;
            if (board.getCell(rc[0], rc[1]) == myColor) friendly++;
        }

        return (total == 0) ? 0.0 : (double) friendly / total;
    }
}











