package AI.AiPlayer;

import Game.Board;
import Game.Color;

/**
 * Shared utilities for converting adapters to Board efficiently.
 */
public final class BoardConverters {
    private BoardConverters() {}

    public static Board toBoard(AIBoardAdapter adapter) {
        int n = adapter.getSize();
        Board b = new Board(n);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                Color cell = adapter.getCell(r, c);
                if (cell == Color.RED) b.getMoveRed(r, c, null);
                else if (cell == Color.BLACK) b.getMoveBlack(r, c, null);
            }
        }
        b.clearMoveHistory();
        return b;
    }
}
