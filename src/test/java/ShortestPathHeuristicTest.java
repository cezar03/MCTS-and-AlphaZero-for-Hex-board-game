import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.Heuristic.ShortestPathHeuristic;
import Game.Board;
import Game.Color;
import Game.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class ShortestPathHeuristicTest {

    private static void place(Board board, int r, int c, Color color) {
        if (color == Color.RED) {
            board.getMoveRed(r, c, Color.RED);
        } else if (color == Color.BLACK) {
            board.getMoveBlack(r, c, Color.BLACK);
        } else {
            throw new IllegalArgumentException("Only RED/BLACK allowed");
        }
    }

    @Test
    void scoreTest_returnsZero_whenDeltaIsZero_symmetricEmptyBoard() {
        Board b = new Board(3);
        GameState state = new GameState(b, Player.RED);

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        double s = h.score(state, new Move(1, 1));

        assertEquals(0.0, s, 1e-12);
    }

    @Test
    void scoreTest_returnsZero_forInvalidMove_edgeCase() {
        Board b = new Board(3);
        place(b, 1, 1, Color.RED);

        GameState state = new GameState(b, Player.RED);
        ShortestPathHeuristic h = new ShortestPathHeuristic();

        double s = h.score(state, new Move(1, 1));
        assertEquals(0.0, s, 1e-12);
    }

    @Test
    void scoreTest_returnsZero_whenStateIsTerminal_edgeCase() {
        Board b = new Board(1);
        place(b, 0, 0, Color.RED);

        GameState state = new GameState(b, Player.RED);
        assertTrue(state.isTerminal());

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        double s = h.score(state, new Move(0, 0));

        assertEquals(0.0, s, 1e-12);
    }

    @Test
    void scoreTest_doesNotMutateOriginalBoard() {
        Board b = new Board(3);
        GameState state = new GameState(b, Player.RED);

        Color before = b.getCell(1, 1);

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        h.score(state, new Move(1, 1));

        assertEquals(before, b.getCell(1, 1));
    }

    @Test
    void scoreTest_resultIsFiniteAndWithinTanhRange() {
        Board b = new Board(3);
        GameState state = new GameState(b, Player.RED);

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        double s = h.score(state, new Move(0, 0));

        assertTrue(Double.isFinite(s));
        assertTrue(s > -1.0 && s < 1.0);
    }

    @Test
    void scoreTest_throwsNullPointer_forNullMove_edgeCase() {
        Board b = new Board(3);
        GameState state = new GameState(b, Player.RED);

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        assertThrows(NullPointerException.class, () -> h.score(state, null));
    }
}
