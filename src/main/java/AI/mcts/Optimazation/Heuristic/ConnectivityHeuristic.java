package AI.mcts.Optimazation.Heuristic;

import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.AiPlayer.AIBoardAdapter;
import Game.Color;
import Game.Player;

/**
 * A heuristic that favors moves adjacent to the current player's existing stones.
 * <p>
 * This heuristic evaluates connectivity by calculating the ratio of friendly
 * neighboring cells to total neighboring cells. Moves with more friendly neighbors
 * receive higher scores, promoting the formation of connected groups and paths.
 * This is particularly valuable in connection games like Hex where building
 * connected paths is the primary objective.
 */
public final class ConnectivityHeuristic  implements Heuristic{

    /**
     * Scores a move based on the proportion of neighboring cells that contain
     * the current player's stones.
     * 
     * @param state the current game state, used to determine the current player
     * @param move the move to be evaluated for connectivity
     * @return the ratio of friendly neighbors to total neighbors in the range [0, 1],
     *         where 1 indicates all neighbors are friendly stones and 0 indicates
     *         no friendly neighbors (or no neighbors exist)
     */
    @Override
    public double score(GameState state, Move move) {
        AIBoardAdapter board = state.getBoard();
        Player toMove = state.getToMove();
        Color myColor = (toMove == Player.RED ? Color.RED : Color.BLACK);
        int friendly = 0;
        int total = 0;
        for (int[] rc : board.neighbors(move.row, move.col)) {
            int r = rc[0];
            int c = rc[1];
            total++;

            if (board.getCell(r, c) == myColor) friendly++;
        }
        return (total == 0) ? 0.0 : (double) friendly / total;
    }
}
