package AI;

import AI.AiPlayer.AIAgent;
import AI.AiPlayer.AITester;
import AI.AiPlayer.MCTSPlayer;
import AI.AiPlayer.RandomPlayer;
import Game.Player;

/**
 * Main class for running AI tests.
 * This class can be modified to quickly test different agent configurations.
 */
public class TestRunner {

    public static void main(String[] args) {
        // Example 1: MCTS vs Random
      // testMCTSvsRandom();

        // Example 2: Different MCTS configurations
       // testMCTSvsMCTS();

        // Example 3: Tournament format (eliminates first-player bias)

        // change for pruning branch: MCTS agent  now takes pruning parameters
        AIAgent mctsAgent1 = new MCTSPlayer(Player.RED, 1000,
                0.05, 0.5, 0.5);  // RED threshold + heuristic weights

        AIAgent mctsAgent2 = new MCTSPlayer(Player.BLACK, 1000,
                0.30, 0.5, 0.5);

        runTournament(mctsAgent1, mctsAgent2, 20, 7, false);
    }

    /**
     * Test MCTS agent against random baseline.
     */
    private static void testMCTSvsRandom() {

        // updated constructor with pruning settings
        AIAgent mcts = new MCTSPlayer(Player.RED, 1000,
                0.25, 0.5, 0.5);

        AIAgent random = new RandomPlayer(Player.BLACK);

        AITester.runMatch(mcts, random, 50, 11, false);
    }

    /**
     * Test two MCTS agents with different iteration counts.
     */
    private static void testMCTSvsMCTS() {

        // allow RED and BLACK to have different pruning settings
        AIAgent mcts1 = new MCTSPlayer(Player.RED, 1000,
                0.25, 0.5, 0.5);     // RED settings

        AIAgent mcts2 = new MCTSPlayer(Player.BLACK, 2000,
                0.8, 0.7, 0.3);     // BLACK settings (different)

        AITester.runMatch(mcts1, mcts2, 30, 11, false);
    }

    /**
     * Run a full tournament to eliminate first-player advantage.
     */
    private static void runTournament(AIAgent agent1, AIAgent agent2,
                                      int gamesPerSide, int boardSize, boolean extensivePrints) {
        AITester.runTournament(agent1, agent2, gamesPerSide, boardSize, extensivePrints);
    }

    /**
     * Quick test with verbose output to see individual game results.
     */
    private static void quickTest(AIAgent agent1, AIAgent agent2) {

        // example with different weights
        AITester.runMatch(agent1, agent2, 5, 11, true);
    }
}
