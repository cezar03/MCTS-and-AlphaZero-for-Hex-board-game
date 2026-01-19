import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.Heuristic.ShortestPathHeuristic;
import Game.Board;
import Game.BoardAdapter;
import Game.BoardAdapter;
import Game.Color;
import Game.Player;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ShortestPathHeuristicTest {

    private static void place(Board board, int r, int c, Color color) {
        if (color == Color.RED) {
            board.getMoveRed(r, c, Color.RED);
        } else if (color == Color.BLACK) {
            board.getMoveBlack(r, c, Color.BLACK);
        } else {
            throw new IllegalArgumentException("Only RED or BLACK stones allowed");
        }
    }

    //Argumwnts factory
    private static GameState newState(Board board, Player toMove) {
        Constructor<?>[] ctors = GameState.class.getConstructors();
        AssertionError lastError = null;

        for (Constructor<?> ctor : ctors) {
            try {
                Object[] args = buildArgs(ctor.getParameterTypes(), board, toMove);
                GameState state = (GameState) ctor.newInstance(args);

                assertNotNull(state);
                assertNotNull(state.getBoard());
                assertNotNull(state.getToMove());

                return state;
            } catch (Throwable t) {
                lastError = new AssertionError(
                        "Tried GameState constructor: " + ctor + " but failed: "
                                + t.getClass().getSimpleName() + " - " + t.getMessage(),
                        t
                );
            }
        }
        throw (lastError != null) ? lastError : new AssertionError("No public constructors found in GameState.");
    }

    private static Object[] buildArgs(Class<?>[] paramTypes, Board board, Player toMove) {
        Object[] args = new Object[paramTypes.length];
        BoardAdapter adapter = null;

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> p = paramTypes[i];

            if (p.isAssignableFrom(Board.class)) {
                args[i] = board;
            } else if (p.isAssignableFrom(BoardAdapter.class)) {
                if (adapter == null) adapter = new BoardAdapter(board);
                args[i] = adapter;
            } else if (p.isAssignableFrom(Player.class)) {
                args[i] = toMove;
            } else if (p.isAssignableFrom(Color.class)) {
                args[i] = toMove.stone;
            } else if (p == int.class || p == Integer.class) {
                args[i] = toMove.id;
            } else if (p == boolean.class || p == Boolean.class) {
                args[i] = false;
            } else if (p == double.class || p == Double.class) {
                args[i] = 0.0;
            } else if (p == long.class || p == Long.class) {
                args[i] = 0L;
            } else if (!p.isPrimitive()) {
                args[i] = null;
            } else {
                if (p == short.class) args[i] = (short) 0;
                else if (p == byte.class) args[i] = (byte) 0;
                else if (p == char.class) args[i] = (char) 0;
                else if (p == float.class) args[i] = 0f;
                else args[i] = 0;
            }
        }
        return args;
    }

    @Test
    void scoreDeltaZeroTest() {
        Board b = new Board(3);
        GameState state = newState(b, Player.RED);

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        double s = h.score(state, new Move(1, 1));

        assertEquals(0.0, s, 1e-12);
    }

    @Test
    void scoreInvalidMoveTest() {
        Board b = new Board(3);
        place(b, 1, 1, Color.RED);

        GameState state = newState(b, Player.RED);
        ShortestPathHeuristic h = new ShortestPathHeuristic();

        double s = h.score(state, new Move(1, 1));
        assertEquals(0.0, s, 1e-12);
    }

    @Test
    void scoreReturnZeroTest() {
        Board b = new Board(1);
        place(b, 0, 0, Color.RED);

        GameState state = newState(b, Player.RED);
        assertTrue(state.isTerminal());

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        double s = h.score(state, new Move(0, 0));

        assertEquals(0.0, s, 1e-12);
    }

    @Test
    void scoreOnOriginalBoardTest() {
        Board b = new Board(3);
        GameState state = newState(b, Player.RED);

        Color before = b.getCell(1, 1);

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        h.score(state, new Move(1, 1));

        assertEquals(before, b.getCell(1, 1));
    }

    @Test
    void scoreFiniteResultTest() {
        Board b = new Board(3);
        GameState state = newState(b, Player.RED);

        ShortestPathHeuristic h = new ShortestPathHeuristic();
        double s = h.score(state, new Move(0, 0));

        assertTrue(Double.isFinite(s));
        assertTrue(s > -1.0 && s < 1.0);
    }

    @Test
    void scoreNullMoveTest() {
        Board b = new Board(3);
        GameState state = newState(b, Player.RED);
        ShortestPathHeuristic h = new ShortestPathHeuristic();
        //null expected
        assertThrows(RuntimeException.class, () -> h.score(state, null));
    }
}
