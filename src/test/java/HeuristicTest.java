import AI.mcts.HexGame.GameState;
import AI.mcts.HexGame.Move;
import AI.mcts.Optimazation.Heuristic.Heuristic;
import Game.Board;
import Game.BoardAdapter;
import Game.Color;
import Game.Player;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.jupiter.api.Assertions.*;

class HeuristicTest {
    //Creating GameState via combining various arguments
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
                        "Tried GameState constructor: " + ctor + " failed:((( "
                                + t.getClass().getSimpleName() + " - " + t.getMessage(),
                        t
                );
            }
        }

        throw (lastError != null) ? lastError : new AssertionError("No public constructors found in GameState class");
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
    void interfaceStructureTest_isInterface() {
        assertTrue(Heuristic.class.isInterface(), "Heuristic must be an interface");
        assertTrue(Modifier.isPublic(Heuristic.class.getModifiers()), "Heuristic should be public");
    }

    @Test
    void scoreMethodSignatureTest_existsAndMatches() throws NoSuchMethodException {
        Method m = Heuristic.class.getMethod("score", GameState.class, Move.class);

        assertEquals(double.class, m.getReturnType(), "score must return double");
        assertTrue(Modifier.isPublic(m.getModifiers()), "score must be public");
        assertTrue(Modifier.isAbstract(m.getModifiers()), "score must be abstract (interface method)");
    }

    @Test
    void polymorphismContractTest_canCallImplementationViaInterface() {
        Heuristic h = (state, move) -> 42.0;
        Board b = new Board(1);
        GameState s = newState(b, Player.RED);
        Move m = new Move(0, 0);
        assertEquals(42.0, h.score(s, m), 0.0);
    }
}
