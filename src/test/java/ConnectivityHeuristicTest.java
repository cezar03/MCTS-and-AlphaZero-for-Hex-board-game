
import ai.mcts.HexGame.GameState;
import game.core.Move;
import ai.mcts.Optimazation.Heuristic.ConnectivityHeuristic;
import game.core.Board;
import bridge.BoardAdapter;
import game.core.Color;
import game.core.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class ConnectivityHeuristicTest {

    private static final ConnectivityHeuristic H = new ConnectivityHeuristic();

    private static void place(Board board, int r, int c, Color color) {
        if (color == Color.RED) board.getMoveRed(r, c, Color.RED);
        else if (color == Color.BLACK) board.getMoveBlack(r, c, Color.BLACK);
        else throw new IllegalArgumentException("Only RED or BLACK allowed");
    }

    /**
     * In order to avoid possible problems with creating GameState for tests we use this GameState "factory"
     * basically, it tries all constructors and auto-builds arguments from board/boardAdapter/player and etc.
     * Extremely helpful in case you merge branches and the arguments differ in game state.
     */
    private static GameState newState(Board board, Player toMove) {
        Constructor<?>[] ctors = GameState.class.getConstructors();
        AssertionError lastError = null;

        for (Constructor<?> ctor : ctors) {
            try {
                Object[] args = buildArgs(ctor.getParameterTypes(), board, toMove);
                Object obj = ctor.newInstance(args);

                GameState state = (GameState) obj;
                assertNotNull(state.getBoard(), "GameState.getBoard() returned null");
                assertNotNull(state.getToMove(), "GameState.getToMove() returned null");
                return state;

            } catch (Throwable t) {
                lastError = new AssertionError(
                        "Tried GameState constructor: " + ctor + " but failed: " + t.getClass().getSimpleName() + " - " + t.getMessage(),
                        t
                );
            }
        }

        throw (lastError != null)
                ? lastError
                : new AssertionError("No public constructors found in GameState.");
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
    void scoreHasNoNeighboursTest() {
        Board b = new Board(1);
        GameState s = newState(b, Player.RED);

        double score = H.score(s, Move.get(0, 0));
        assertEquals(0.0, score, 0.0);
    }

    @Test
    void scoreNoFriendNeighboursTest() {
        Board b = new Board(3);
        GameState s = newState(b, Player.RED);
        double score = H.score(s, Move.get(1, 1));
        assertEquals(0.0, score, 0.0);
    }

    @Test
    void scoreThreeFriendlyNeighboursTest() {
        Board b = new Board(3);
        GameState s = newState(b, Player.RED);
        place(b, 0, 1, Color.RED);
        place(b, 1, 0, Color.RED);
        place(b, 2, 1, Color.RED);
        int total = b.neighbors(1, 1).size();
        assertTrue(total > 0);

        double expected = 3.0 / total;
        double score = H.score(s, Move.get(1, 1));
        assertEquals(expected, score, 1e-12);
    }

    @Test
    void scoreAllFriendlyNeighboursTest() {
        Board b = new Board(3);
        GameState s = newState(b, Player.RED);
        for (int[] nb : b.neighbors(1, 1)) {
            place(b, nb[0], nb[1], Color.RED);
        }
        double score = H.score(s, Move.get(1, 1));
        assertEquals(1.0, score, 1e-12);
    }

    @Test
    void scoreCountCornersCorrectlyTest() {
        Board b = new Board(3);
        GameState s = newState(b, Player.RED);

        int total = b.neighbors(0, 0).size();
        assertTrue(total > 0 && total < 6);

        int[] nb = b.neighbors(0, 0).get(0);
        place(b, nb[0], nb[1], Color.RED);

        double score = H.score(s, Move.get(0, 0));
        assertEquals(1.0 / total, score, 1e-12);
    }

    @Test
    void scoreMoveColorTest() {
        Board b = new Board(3);
        GameState s = newState(b, Player.BLACK);

        place(b, 0, 1, Color.BLACK);
        place(b, 1, 0, Color.BLACK);

        place(b, 2, 1, Color.RED);
        place(b, 1, 2, Color.RED);

        int total = b.neighbors(1, 1).size();
        double expected = 2.0 / total;

        double score = H.score(s, Move.get(1, 1));
        assertEquals(expected, score, 1e-12);
    }
    //Should return the score for a certain player and the num of their stones near on the board
    @Test
    void scoreReturnDifferentScoresTest() {
        Board b = new Board(3);

        place(b, 0, 1, Color.RED);
        place(b, 1, 0, Color.BLACK);

        int total = b.neighbors(1, 1).size();

        GameState redState = newState(b, Player.RED);
        GameState blackState = newState(b, Player.BLACK);

        double redScore = H.score(redState, Move.get(1, 1));
        double blackScore = H.score(blackState, Move.get(1, 1));

        assertEquals(1.0 / total, redScore, 1e-12);
        assertEquals(1.0 / total, blackScore, 1e-12);
    }
}









