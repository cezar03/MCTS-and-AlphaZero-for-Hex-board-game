package AI;

import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AITester;
import AI.AiPlayer.MCTSPlayer;
import AI.AiPlayer.RandomPlayer;
import Game.Player;

public class TestRunner {

    // tuned config from your tuner (thr, cent, conn, bias, sp)
    private static final double THR   = 0.5411830438399008;
    private static final double CENT  = 0.05311371878045057;
    private static final double CONN  = 0.9468862812195494;
    private static final double BIAS  = 0.036;
    private static final double SP    = 0.033;

    private static final int ITERS    = 1000;
    private static final int BOARD_SZ = 7;

    public static void main(String[] args) {
        sweepCValues();
        // testMCTSvsRandom();
        // testMCTSvsMCTS();
    }

    /**
     * Sweep over different exploration constants c and run a tournament
     * for each value: base MCTS vs tuned/pruned MCTS.
     */
    private static void sweepCValues() {
        double[] cValues = {
                0.5,
                0.8,
                1.0,
                Math.sqrt(1.4),
                2.0
        };

        int gamesPerSide = 50;

        for (double c : cValues) {
            System.out.printf("%n===== Testing c = %.5f =====%n", c);
            AIAgent base = new MCTSPlayer(Player.RED, ITERS);
            AIAgent opt  = new MCTSPlayer(Player.BLACK, ITERS,
                    THR, CENT, CONN,
                    BIAS, SP,
                    c);
            AITester.runTournament(base, opt, gamesPerSide, BOARD_SZ, false);
        }
    }

    /**
     * Test tuned MCTS against random baseline (optional).
     */
    private static void testMCTSvsRandom() {
        AIAgent mcts = new MCTSPlayer(Player.RED, ITERS,
                THR, CENT, CONN,
                BIAS, SP,
                1.0
        );

        AIAgent random = new RandomPlayer(Player.BLACK);
        AITester.runMatch(mcts, random, 50, 11, false);
    }

    /**
     * Base vs tuned MCTS, single c value (optional).
     */
    private static void testMCTSvsMCTS() {
        AIAgent base = new MCTSPlayer(Player.RED, ITERS);

        AIAgent tuned = new MCTSPlayer(Player.BLACK, ITERS,
                THR, CENT, CONN,
                BIAS, SP,
                1.0
        );

        AITester.runMatch(base, tuned, 30, 11, false);
    }
}
