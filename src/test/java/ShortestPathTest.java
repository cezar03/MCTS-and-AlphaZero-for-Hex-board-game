import AI.mcts.Optimazation.ShortestPath;
import game.core.Board;
import game.core.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortestPathTest {

    private static final int INF = 1_000_000;

    private static void place(Board board, int r, int c, Color color) {
        if (color == Color.RED) {
            board.getMoveRed(r, c, Color.RED);
        } else if (color == Color.BLACK) {
            board.getMoveBlack(r, c, Color.BLACK);
        } else {
            throw new IllegalArgumentException("Only RED or BLACK allowed");
        }
    }

    private static int sp(Board board, Color player) {
        return ShortestPath.shortestPath(board, player);
    }

    @Test
    void size1Empty_returnsOneTest() {
        Board b = new Board(1);

        assertEquals(1, sp(b, Color.RED));
        assertEquals(1, sp(b, Color.BLACK));
    }

    @Test
    void costZeroTest() {
        Board red = new Board(1);
        place(red, 0, 0, Color.RED);
        assertEquals(0, sp(red, Color.RED));

        Board black = new Board(1);
        place(black, 0, 0, Color.BLACK);
        assertEquals(0, sp(black, Color.BLACK));
    }

    @Test
    void opponentStoneBlockedTest() {
        Board b1 = new Board(1);
        place(b1, 0, 0, Color.BLACK);
        assertEquals(INF, sp(b1, Color.RED));

        Board b2 = new Board(1);
        place(b2, 0, 0, Color.RED);
        assertEquals(INF, sp(b2, Color.BLACK));
    }

    @Test
    void empty2x2_returnsTwoTest() {
        Board b = new Board(2);

        assertEquals(2, sp(b, Color.RED));
        assertEquals(2, sp(b, Color.BLACK));
    }

    @Test
    void redStoneOnTopReduceCostTest() {
        Board b = new Board(2);
        place(b, 0, 0, Color.RED);

        assertEquals(1, sp(b, Color.RED));
    }

    @Test
    void blackStoneOnLeftReduceCostTest() {
        Board b = new Board(2);
        place(b, 0, 0, Color.BLACK);

        assertEquals(1, sp(b, Color.BLACK));
    }

    @Test
    void redConnectedChainReturnsZeroTest() {
        Board b = new Board(3);

        place(b, 0, 1, Color.RED);
        place(b, 1, 1, Color.RED);
        place(b, 2, 1, Color.RED);

        assertEquals(0, sp(b, Color.RED));
    }

    @Test
    void blackConnectedChainReturnsZeroTest() {
        Board b = new Board(3);

        place(b, 1, 0, Color.BLACK);
        place(b, 1, 1, Color.BLACK);
        place(b, 1, 2, Color.BLACK);

        assertEquals(0, sp(b, Color.BLACK));
    }

    @Test
    void opponentBlocksAllTargetsRedTest() {
        Board b = new Board(2);

        place(b, 1, 0, Color.BLACK);
        place(b, 1, 1, Color.BLACK);

        assertEquals(INF, sp(b, Color.RED));
    }

    @Test
    void opponentBlocksAllTargetsBlackTest() {
        Board b = new Board(2);

        place(b, 0, 1, Color.RED);
        place(b, 1, 1, Color.RED);

        assertEquals(INF, sp(b, Color.BLACK));
    }

    @Test
    void countsOnlyEmptyCellsTest() {
        Board b = new Board(3);

        place(b, 0, 0, Color.RED);
        place(b, 1, 0, Color.RED);
        // (2,0) empty -> cost should be 1

        assertEquals(1, sp(b, Color.RED));
    }

    @Test
    void doNotModifyBoardTest() {
        Board b = new Board(2);

        Color before = b.getCell(0, 0);
        sp(b, Color.RED);
        Color after = b.getCell(0, 0);

        assertEquals(before, after);
    }
}









