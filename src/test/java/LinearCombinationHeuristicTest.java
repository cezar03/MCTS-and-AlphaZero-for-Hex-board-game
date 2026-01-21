import game.core.Move;
import AI.mcts.Optimazation.Heuristic.Heuristic;
import AI.mcts.Optimazation.Heuristic.LinearCombinationHeuristic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinearCombinationHeuristicTest {

    @Test
    void scoreWeightedSumtest() {
        Heuristic cent = (s, m) -> 2.0;
        Heuristic conn = (s, m) -> 3.0;

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, conn, null, 10.0, 1.0, 999.0);
        double score = h.score(null, Move.get(0, 0));
        assertEquals(10.0 * 2.0 + 1.0 * 3.0, score, 0.0);
    }

    @Test
    void scoreNotNullNotZeroTest() {
        Heuristic cent = (s, m) -> 1.0;
        Heuristic conn = (s, m) -> 2.0;
        Heuristic sp   = (s, m) -> 5.0;

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, conn, sp, 1.0, 1.0, 0.1);

        double score = h.score(null, Move.get(1, 1));
        assertEquals(1.0 * 1.0 + 1.0 * 2.0 + 0.1 * 5.0, score, 1e-12);
    }

    @Test
    void scoreWeightZeroTest() {

        Heuristic cent = (s, m) -> 1.0;
        Heuristic conn = (s, m) -> 1.0;
        Heuristic sp = (s, m) -> { throw new AssertionError("Do not call shortestpath!!"); };

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, conn, sp, 1.0, 1.0, 0.0);

        double score = h.score(null, Move.get(0, 0));
        assertEquals(2.0, score, 0.0);
    }

    @Test
    void scoreWeightsZeroTest() {
        Heuristic cent = (s, m) -> 100.0;
        Heuristic conn = (s, m) -> -50.0;
        Heuristic sp   = (s, m) -> 999.0;

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, conn, sp, 0.0, 0.0, 0.0);

        assertEquals(0.0, h.score(null, Move.get(2, 2)), 0.0);
    }

    @Test
    void scoreNegativeWeightsTest() {
        Heuristic cent = (s, m) -> 2.0;
        Heuristic conn = (s, m) -> 3.0;
        Heuristic sp   = (s, m) -> 4.0;
        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, conn, sp, -1.0, 2.0, -0.5);
        double score = h.score(null, Move.get(0, 0));
        assertEquals((-1.0) * 2.0 + 2.0 * 3.0 + (-0.5) * 4.0, score, 1e-12);
    }

    @Test
    void scalingWeightsTest() {
        Heuristic cent = (s, m) -> 1.5;
        Heuristic conn = (s, m) -> -2.0;
        Heuristic sp   = (s, m) -> 10.0;

        LinearCombinationHeuristic h1 =
                new LinearCombinationHeuristic(cent, conn, sp, 1.0, 2.0, 0.1);

        LinearCombinationHeuristic h2 =
                new LinearCombinationHeuristic(cent, conn, sp, 2.0, 4.0, 0.2);

        Move mv = Move.get(1, 2);
        double s1 = h1.score(null, mv);
        double s2 = h2.score(null, mv);

        assertEquals(2.0 * s1, s2, 1e-12);
    }

    @Test
    void nanTest() {
        Heuristic cent = (s, m) -> Double.NaN;
        Heuristic conn = (s, m) -> 1.0;

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, conn, null, 1.0, 1.0, 0.0);

        double score = h.score(null, Move.get(0, 0));
        assertTrue(Double.isNaN(score), "NaN may affect the final score");
    }

    @Test
    void infinityTest() {
        Heuristic cent = (s, m) -> Double.POSITIVE_INFINITY;
        Heuristic conn = (s, m) -> 1.0;
        Heuristic sp   = (s, m) -> 2.0;

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, conn, sp, 1.0, 1.0, 1.0);

        double score = h.score(null, Move.get(0, 0));
        assertTrue(Double.isInfinite(score));
        assertTrue(score > 0);
    }

    @Test
    void nullPointerTest() {
        Heuristic conn = (s, m) -> 1.0;

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(null, conn, null, 1.0, 1.0, 0.0);

        assertThrows(NullPointerException.class, () -> h.score(null, Move.get(0, 0)));
    }

    @Test
    void connectivityNullTest() {
        Heuristic cent = (s, m) -> 1.0;

        LinearCombinationHeuristic h =
                new LinearCombinationHeuristic(cent, null, null, 1.0, 1.0, 0.0);

        assertThrows(NullPointerException.class, () -> h.score(null, Move.get(0, 0)));
    }
}










