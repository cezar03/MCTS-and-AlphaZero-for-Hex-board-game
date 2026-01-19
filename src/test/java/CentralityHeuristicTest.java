import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.Heuristic.CentralityHeuristic;
import Game.Board;
import Game.BoardAdapter;
import Game.Color;
import Game.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class CentralityHeuristicTest {
    //Arguments factory
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
    void scoreCenterMoveTest() {
        Board b = new Board(5); // center = (2,2)
        GameState state = newState(b, Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double s = h.score(state, new Move(2, 2));
        assertEquals(1.0, s, 1e-12);
    }

    @Test
    void scoreCornerIsLowerThanNearCenterTest() {
        Board b = new Board(5);
        GameState state = newState(b, Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double corner = h.score(state, new Move(0, 0));
        double nearCenter = h.score(state, new Move(2, 1));

        assertTrue(corner >= 0.0, "Central scores should not be negative");
        assertTrue(nearCenter > corner, "Moves closer to the center have to be scored higher than corner ones");
    }

    @Test
    void scoreCloseCenterTest() {
        Board b = new Board(5);
        GameState state = newState(b, Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double center = h.score(state, new Move(2, 2));
        double near   = h.score(state, new Move(2, 1));
        double far    = h.score(state, new Move(0, 0));

        assertTrue(center > near);
        assertTrue(near > far);
    }

    @Test
    void scoreOnSymmetricBoardTest() {
        Board b = new Board(5); // center (2,2)
        GameState state = newState(b, Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double top = h.score(state, new Move(0, 2));
        double bot = h.score(state, new Move(4, 2));

        assertEquals(top, bot, 1e-12);
    }

    @Test
    void scoreEvenBoardTest() {
        Board b = new Board(4); //center between (1,1),(1,2),(2,1),(2,2)
        GameState state = newState(b, Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double s11 = h.score(state, new Move(1, 1));
        double s22 = h.score(state, new Move(2, 2));

        assertEquals(s11, s22, 1e-12);
        assertTrue(s11 <= 1.0, "Score must be <= 1.0");
        assertTrue(s11 < 1.0, "On even-sized board, no single cell is the exact center, then score should be < 1.0");
    }

    @Test
    void scoreRangeTest() {
        Board b = new Board(6);
        GameState state = newState(b, Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double s1 = h.score(state, new Move(0, 0));
        double s2 = h.score(state, new Move(5, 5));
        double s3 = h.score(state, new Move(3, 2));

        assertTrue(s1 >= 0.0 && s1 <= 1.0);
        assertTrue(s2 >= 0.0 && s2 <= 1.0);
        assertTrue(s3 >= 0.0 && s3 <= 1.0);
    }

    @Test
    void outOfBoundsTest() {
        Board b = new Board(5);
        GameState state = newState(b, Player.RED);
        CentralityHeuristic h = new CentralityHeuristic();

        double s = h.score(state, new Move(-1, 0));
        assertTrue(s < 0.0, "move out of bounds - then negative score");
    }
}
