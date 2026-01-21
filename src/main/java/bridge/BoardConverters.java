package bridge;

import AI.api.AIBoardAdapter;
import game.core.Board;
import game.core.Color;

/**
 * Shared utilities for converting adapters to Board efficiently.
 */
public final class BoardConverters {
    private BoardConverters() {}

    /**
     * Converts an abstract {@link AIBoardAdapter} back into a concrete {@link Board} instance.
     * <p>
     * This constructs a new Board and populates it with moves corresponding to the 
     * state of the adapter.
     * * @param adapter the source adapter
     * @return a new Board instance reflecting the adapter's state
     */
    public static Board toBoard(AIBoardAdapter adapter) {
        int n = adapter.getSize();
        Board board = new Board(n);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                Color cell = adapter.getCell(r, c);
                if (cell == Color.RED) board.getMoveRed(r, c, null);
                else if (cell == Color.BLACK) board.getMoveBlack(r, c, null);
            }
        }
        board.clearMoveHistory();
        return board;
    }
}











